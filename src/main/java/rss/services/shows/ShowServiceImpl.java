package rss.services.shows;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.EpisodesComparator;
import rss.controllers.EntityConverter;
import rss.controllers.vo.EpisodeSearchResult;
import rss.controllers.vo.ShowScheduleEpisodeItem;
import rss.controllers.vo.ShowsScheduleVO;
import rss.controllers.vo.UserTorrentVO;
import rss.dao.*;
import rss.entities.*;
import rss.services.EmailService;
import rss.services.PageDownloader;
import rss.services.SettingsService;
import rss.services.downloader.DownloadResult;
import rss.services.downloader.TVShowsTorrentEntriesDownloader;
import rss.services.log.LogService;
import rss.services.requests.EpisodeRequest;
import rss.services.requests.ShowRequest;
import rss.services.requests.SingleEpisodeRequest;
import rss.util.CollectionUtils;
import rss.util.DateUtils;
import rss.util.DurationMeter;
import rss.util.MultiThreadExecutor;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: dikmanm
 * Date: 05/01/13 18:01
 */
@Service
public class ShowServiceImpl implements ShowService {

	private static final int MAX_CONCURRENT_SHOWS = 10;
	private static final int MAX_DID_YOU_MEAN = 20;

	@Autowired
	private ShowDao showDao;

	@Autowired
	private UserDao userDao;

	private TVRageServiceImpl showsProvider;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private TVShowsTorrentEntriesDownloader torrentEntriesDownloader;

	@Autowired
	private TorrentDao torrentDao;

	@Autowired
	private UserTorrentDao userTorrentDao;

	@Autowired
	private EntityConverter entityConverter;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private EpisodeDao episodeDao;

	@Autowired
	private LogService logService;

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	private ShowsCacheService showsCacheService;

	@Autowired
	private EmailService emailService;

	@PostConstruct
	private void postConstruct() {
		showsProvider = new TVRageServiceImpl();
		showsProvider.setLogService(logService);
		showsProvider.setPageDownloader(pageDownloader);
	}

	private void saveNewShow(Show show) {
		logService.info(getClass(), "It is a new show! - Persisting '" + show.getName() + "' (tvrage_id=" + show.getTvRageId() + ")");
		showDao.persist(show);
		showsCacheService.put(show);
		Collection<Episode> episodes = showsProvider.downloadSchedule(show);
		for (Episode episode : episodes) {
			episodeDao.persist(episode);
			episode.setShow(show);
			show.getEpisodes().add(episode);
		}
	}

	// Levenshtein distance (LD)
	// Don't use threshold, cuz maybe our name is shorter than the actual name... like spartacus: ....
	// and we search simply for spartacus
	public Collection<Show> statisticMatch(String name) {
		name = normalize(name);
		List<String> sortedNameSplit = Arrays.asList(name.split(" "));
		final int nameWords = sortedNameSplit.size();
		Collections.sort(sortedNameSplit);
		final String sortedNameJoined = StringUtils.join(sortedNameSplit.toArray(), " ");

		// lock for matches and bestLD
		final Lock lock = new ReentrantLock();
		final Set<CachedShow> matches = new HashSet<>();
		final MutableInt bestLD = new MutableInt(Integer.MAX_VALUE);
		// 5 is best from tries on my laptop (tried 1, 5, 10, 20, 30)
		MultiThreadExecutor.execute(Executors.newFixedThreadPool(5), showsCacheService.getShowsSubsets(), logService,
				new MultiThreadExecutor.MultiThreadExecutorTask<Map.Entry<CachedShow, Collection<CachedShowSubset>>>() {
					@Override
					public void run(Map.Entry<CachedShow, Collection<CachedShowSubset>> entry) {
						CachedShow show = entry.getKey();
						if (show.getWords() < nameWords) {
							// if show has less words that the search term - it doesn't match
							return;
						}

						int ld = Integer.MAX_VALUE;
						for (CachedShowSubset subset : entry.getValue()) {
							// no point doing contains if there are less words in the subset than in the search term
							if (subset.getWords() >= nameWords && subset.getSubset().contains(sortedNameJoined)) {
								ld = 0;
							} else {
								int curLd = StringUtils.getLevenshteinDistance(sortedNameJoined, subset.getSubset());
								if (curLd != -1) {
									ld = Math.min(curLd, ld);
								}
							}
						}

						lock.lock();
						if (ld < bestLD.getValue()) {
							matches.clear();
						}

						// if ld same as bestLD still want to add the show
						if (matches.isEmpty() || ld <= bestLD.getValue()) {
							matches.add(show);
							bestLD.setValue(ld);
							logService.debug(getClass(), "show=" + show.getName() + " ld=" + ld);
						}
						lock.unlock();
					}
				});

		List<CachedShow> matchesList = new ArrayList<>(matches);

		// if found too many
		if (matches.size() > MAX_DID_YOU_MEAN) {
			Collections.sort(matchesList, new Comparator<CachedShow>() {
				@Override
				public int compare(CachedShow o1, CachedShow o2) {
					return Integer.valueOf(o1.getWords()).compareTo(o2.getWords());
				}
			});
		}

		Collection<Show> result = new ArrayList<>();
		for (CachedShow match : matchesList.subList(0, Math.min(matchesList.size(), MAX_DID_YOU_MEAN))) {
			result.add(showDao.find(match.getId()));
		}

		logService.debug(getClass(), "Show statistic match end for: " + name + " found: " + StringUtils.join(result.toArray(), ","));
		return result;
	}

	public static String normalize(String name) {
		name = name.toLowerCase();
		name = name.replaceAll("['\"!,&\\-\\+\\?/:]", "");
		name = name.replaceAll("[\\.]", " ");
		return name;
	}

	@Override
	// substitute show name with alias and seasons if needed
	public void transformEpisodeRequest(ShowRequest showRequest) {
		String alias = settingsService.getShowAlias(showRequest.getShow().getName());
		if (!StringUtils.isBlank(alias)) {
			showRequest.setTitle(alias);

			if (showRequest instanceof EpisodeRequest) {
				EpisodeRequest episodeRequest = (EpisodeRequest) showRequest;
				episodeRequest.setSeason(settingsService.getShowSeasonAlias(showRequest.getShow().getName(), episodeRequest.getSeason()));
			}
		}
	}

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void downloadShowList() {
		final Class aClass = getClass();
		Collection<Show> downloadedShows = showsProvider.downloadShowList();

		ExecutorService executorService = Executors.newFixedThreadPool(MAX_CONCURRENT_SHOWS);
		for (final Show downloadedShow : downloadedShows) {
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						transactionTemplate.execute(new TransactionCallbackWithoutResult() {
							@Override
							protected void doInTransactionWithoutResult(TransactionStatus arg0) {
								Show show = showDao.findByName(downloadedShow.getName());
								if (show == null) {
									saveNewShow(downloadedShow);
								} else {
									// update existing shows with tvrage id
									show.setTvRageId(downloadedShow.getTvRageId());

									// update show status that might have changed
									show.setEnded(downloadedShow.isEnded());
								}
							}
						});
					} catch (Exception e) {
						logService.error(aClass, "Failed downloading info for show '" + downloadedShow.getName() + "': " + e.getMessage(), e);
					}
				}
			});
		}

		executorService.shutdown();
		try {
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			logService.error(getClass(), "Error waiting for download tasks to execute: " + e.getMessage(), e);
		}
	}

	@Override
	public EpisodeSearchResult search(ShowRequest episodeRequest, User user) {
		// saving original search term - it might change during the search
		String originalSearchTerm = episodeRequest.getTitle();
		String actualSearchTerm;

		DurationMeter duration = new DurationMeter();
		Collection<Show> didYouMeanShows = statisticMatch(originalSearchTerm);
		duration.stop();
		logService.info(getClass(), "Did you mean time - " + duration.getDuration());

		// first check which show we need
		Show show = showDao.findByName(originalSearchTerm);
		if (show != null) {
			episodeRequest.setShow(show);
			episodeRequest.setTitle(show.getName());
			actualSearchTerm = originalSearchTerm = show.getName();
			didYouMeanShows.remove(show); // don't show this show as did you mean, already showing results for it
		} else if (didYouMeanShows.isEmpty()) {
			return EpisodeSearchResult.createNoResults(originalSearchTerm);
		} else if (didYouMeanShows.size() == 1) {
			Show didYouMeanShow = didYouMeanShows.iterator().next();
			episodeRequest.setShow(didYouMeanShow);
			episodeRequest.setTitle(didYouMeanShow.getName());
			actualSearchTerm = didYouMeanShow.getName();
			if (actualSearchTerm.equalsIgnoreCase(originalSearchTerm)) {
				originalSearchTerm = actualSearchTerm;
			}
			didYouMeanShows = Collections.emptyList();
		} else {
			return EpisodeSearchResult.createDidYouMean(originalSearchTerm, entityConverter.toThinShows(didYouMeanShows));
		}

		ArrayList<UserTorrentVO> result = new ArrayList<>();
		Collection<Episode> downloaded = torrentEntriesDownloader.download(Collections.singleton(episodeRequest)).getDownloaded();

		Map<Torrent, Episode> episodeByTorrents = new HashMap<>();
		final Map<Long, Episode> episodeByTorrentsForComparator = new HashMap<>();
		for (Episode episode : downloaded) {
			for (Long torrentId : episode.getTorrentIds()) {
				Torrent torrent = torrentDao.find(torrentId);
				episodeByTorrents.put(torrent, episode);
				episodeByTorrentsForComparator.put(torrent.getId(), episode);
			}
		}

		// add those containing user torrent
		for (UserTorrent userTorrent : userTorrentDao.findUserEpisodes(downloaded, user)) {
			Torrent torrent = userTorrent.getTorrent();
			episodeByTorrents.remove(torrent);
			UserTorrentVO userTorrentVO = new UserTorrentVO()
					.withTitle(torrent.getTitle())
					.withTorrentId(torrent.getId())
					.withDownloaded(true);
			result.add(userTorrentVO);
		}

		// add the rest of the episodes
		for (Torrent torrent : episodeByTorrents.keySet()) {
			UserTorrentVO userTorrentVO = new UserTorrentVO()
					.withTitle(torrent.getTitle())
					.withTorrentId(torrent.getId())
					.withDownloaded(false);
			result.add(userTorrentVO);
		}

		final EpisodesComparator episodesComparator = new EpisodesComparator();
		Collections.sort(result, new Comparator<UserTorrentVO>() {
			@Override
			public int compare(UserTorrentVO o1, UserTorrentVO o2) {
				Episode episode1 = episodeByTorrentsForComparator.get(o1.getTorrentId());
				Episode episode2 = episodeByTorrentsForComparator.get(o2.getTorrentId());
				return episodesComparator.compare(episode1, episode2);
			}
		});

		EpisodeSearchResult episodeSearchResult = new EpisodeSearchResult(originalSearchTerm, actualSearchTerm, result);
		episodeSearchResult.setDidYouMean(entityConverter.toThinShows(didYouMeanShows));

		return episodeSearchResult;
	}

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void downloadSchedule(final Show nonTransactionShow) {
		// must separate schedule download and torrent download into separate transactions
		// cuz in the first creating episodes which must be available (committed) in the second part
		// and the second part spawns separate threads and transactions
		final DownloadScheduleResult downloadScheduleResult = transactionTemplate.execute(new TransactionCallback<DownloadScheduleResult>() {
			@Override
			public DownloadScheduleResult doInTransaction(TransactionStatus arg0) {
				// need to requery so it will be in this transaction
				Show show = showDao.find(nonTransactionShow.getId());
				Collection<Episode> episodes = showsProvider.downloadSchedule(show);
				DownloadScheduleResult downloadScheduleResult = new DownloadScheduleResult();
				downloadScheduleResultHelper(show, episodes, downloadScheduleResult);
				return downloadScheduleResult;
			}
		});

		downloadScheduleHelper(downloadScheduleResult);
	}

	private void downloadScheduleHelper(DownloadScheduleResult downloadScheduleResult) {
		// download torrents for the new episodes
		final Set<ShowRequest> episodesToDownload = new HashSet<>();
		for (Episode episode : downloadScheduleResult.getNewEpisodes()) {
			Show show = episode.getShow();
			episodesToDownload.add(new SingleEpisodeRequest(show.getName(), show, MediaQuality.HD720P, episode.getSeason(), episode.getEpisode()));
			logService.info(getClass(), "Will try to download torrents of " + episode);
		}

		final Set<ShowRequest> missing = new HashSet<>();
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus arg0) {
				DownloadResult<Episode, ShowRequest> downloadResult = torrentEntriesDownloader.download(episodesToDownload);
				missing.addAll(downloadResult.getMissing());
			}
		});

		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus arg0) {
				// if missing episode is released today (no matter the hour) then don't email it
				for (ShowRequest episodeRequest : new ArrayList<>(missing)) {
					for (Episode episode : episodeDao.find((EpisodeRequest) episodeRequest)) {
						// might be null cuz maybe there is no such episode at all - who knows what they search for
						if (/*episode != null &&*/ episode.getAirDate() != null && DateUtils.isToday(episode.getAirDate())) {
							missing.remove(episodeRequest);
						}
					}
				}
				emailService.notifyOfMissingEpisodes(missing);
			}
		});
	}

	@Override
	public List<AutoCompleteItem> autoCompleteShowNames(String term, boolean includeEnded, Predicate<? super AutoCompleteItem> predicate) {
		term = term.toLowerCase().trim();
		List<AutoCompleteItem> result = new ArrayList<>();
		for (CachedShow cachedShow : showsCacheService.getAll()) {
			if ((includeEnded || !cachedShow.isEnded()) &&
				(cachedShow.getName().toLowerCase().contains(term))) {
				result.add(new AutoCompleteItem(cachedShow.getId(), cachedShow.getName()));
			}
		}
		if (predicate != null) {
			result = new ArrayList<>(Collections2.filter(result, predicate));
		}
		return result;
	}

	@Override
	public ShowsScheduleVO getSchedule(Set<Show> shows) {
		List<Long> showIds = new ArrayList<>();
		for (Show show : shows) {
			showIds.add(show.getId());
		}

		Collection<Episode> episodes = episodeDao.getEpisodesForSchedule(showIds);
		Map<Date, Set<ShowScheduleEpisodeItem>> map = new HashMap<>();
		// add today
		map.put(new Date(), new HashSet<ShowScheduleEpisodeItem>());

		for (Episode episode : episodes) {
			Date episodeDate = episode.getAirDate();
			Date date = null;
			for (Date curDate : map.keySet()) {
				if (DateUtils.isSameDay(episodeDate, curDate)) {
					date = curDate;
					break;
				}
			}
			if (date == null) {
				date = episodeDate;
				map.put(date, new HashSet<ShowScheduleEpisodeItem>());
			}
			map.get(date).add(new ShowScheduleEpisodeItem(episode.getSeasonEpisode().toUpperCase(), episode.getShow().getName()));
		}

		// sort the dates, newest at beginning
		List<Date> dates = new ArrayList<>(map.keySet());
		Collections.sort(dates);
		Collections.reverse(dates);

		ShowsScheduleVO schedule = new ShowsScheduleVO();
		for (Date date : dates) {
			List<ShowScheduleEpisodeItem> showNames = new ArrayList<>(map.get(date));
			Collections.sort(showNames, new Comparator<ShowScheduleEpisodeItem>() {
				@Override
				public int compare(ShowScheduleEpisodeItem o1, ShowScheduleEpisodeItem o2) {
					int res = o1.getShowName().compareToIgnoreCase(o2.getShowName());
					if (res != 0) {
						return res;
					}

					return o1.getSequence().compareToIgnoreCase(o2.getSequence());
				}
			});
			schedule.addSchedule(date, showNames);
		}
		return schedule;
	}

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public DownloadScheduleResult downloadSchedule() {
		// must separate schedule download and torrent download into separate transactions
		// cuz in the first creating episodes which must be available (committed) in the second part
		// and the second part spawns separate threads and transactions
		final DownloadScheduleResult downloadScheduleResult = transactionTemplate.execute(new TransactionCallback<DownloadScheduleResult>() {
			@Override
			public DownloadScheduleResult doInTransaction(TransactionStatus arg0) {
				DownloadScheduleResult downloadScheduleResult = new DownloadScheduleResult();

				// collect all future episode schedules as given by the showsProvider
				Map<Show, List<Episode>> map = new HashMap<>();
				Collection<Episode> episodes = showsProvider.downloadSchedule();
				for (Episode episode : episodes) {
					CollectionUtils.safeListPut(map, episode.getShow(), episode);
				}

				for (Map.Entry<Show, List<Episode>> entry : map.entrySet()) {
					Show showShell = entry.getKey();
					List<Episode> curEpisodes = entry.getValue();

					try {
						Show show = showDao.findByName(showShell.getName());
						// if show is not found, it is not being tracked
						if (show == null) {
							saveNewShow(showShell);
							continue;
						} else if (show.getTvRageId() == -1) {
							show.setTvRageId(showShell.getTvRageId());
						}
						if (!userDao.isShowBeingTracked(show)) {
							continue;
						}

						downloadScheduleResultHelper(show, curEpisodes, downloadScheduleResult);
					} catch (Exception e) {
						downloadScheduleResult.addFailedShow(showShell);
						// why before there was no exception trace printed to log?
						logService.error(getClass(), "Failed downloading schedule for show " + showShell + " " + e.getMessage(), e);
					}
				}

				return downloadScheduleResult;
			}
		});

		downloadScheduleHelper(downloadScheduleResult);
		return downloadScheduleResult;
	}

	private void downloadScheduleResultHelper(Show show, Collection<Episode> episodes, DownloadScheduleResult downloadScheduleResult) {
		logService.info(getClass(), "Downloading schedule for '" + show + "'");

		EpisodesMapper mapper = new EpisodesMapper(show.getEpisodes());
		for (Episode episode : episodes) {
			Episode persistedEpisode = mapper.get(episode.getSeason(), episode.getEpisode());
			if (persistedEpisode == null) {
				episodeDao.persist(episode);
				episode.setShow(show);
				show.getEpisodes().add(episode);
				downloadScheduleResult.addNewEpisode(episode);
				persistedEpisode = episode;
			}
			persistedEpisode.setAirDate(episode.getAirDate());
		}

		// in addition we need past episodes, which might have no torrents for some reason (maybe job was broken or something)
		for (Episode episode : show.getEpisodes()) {
			if (episode.getTorrentIds().isEmpty()) {
				// if there are episodes without torrents, try to find their torrents
				downloadScheduleResult.addNewEpisode(episode);
			}
		}
	}

	@Override
	public boolean isMatch(EpisodeRequest mediaRequest, String title) {
		// need the space to avoid matching housewives to house
		String requestTitle = ShowServiceImpl.normalize(mediaRequest.getTitle()).toLowerCase() + " ";
		String seasonEpisode = mediaRequest.getSeasonEpisode();
		title = ShowServiceImpl.normalize(title.toLowerCase());
		// startswith to avoid matching fullhouse to house
		return title.startsWith(requestTitle) && title.contains(seasonEpisode);
	}
}
