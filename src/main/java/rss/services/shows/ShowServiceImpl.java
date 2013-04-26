package rss.services.shows;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.primitives.Ints;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.NonUniqueResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.EpisodesComparator;
import rss.controllers.vo.ShowScheduleEpisodeItem;
import rss.controllers.vo.ShowsScheduleVO;
import rss.dao.*;
import rss.entities.*;
import rss.services.EmailService;
import rss.services.PageDownloader;
import rss.services.SettingsService;
import rss.services.downloader.DownloadResult;
import rss.services.downloader.TVShowsTorrentEntriesDownloader;
import rss.services.log.LogService;
import rss.services.requests.EpisodeRequest;
import rss.services.requests.FullSeasonRequest;
import rss.services.requests.ShowRequest;
import rss.services.requests.SingleEpisodeRequest;
import rss.util.CollectionUtils;
import rss.util.DateUtils;
import rss.util.Utils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * User: dikmanm
 * Date: 05/01/13 18:01
 */
@Service
public class ShowServiceImpl implements ShowService {

	private static final int MAX_CONCURRENT_SHOWS = 10;

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

	@Autowired
	private ShowSearchService showSearchService;

	@Autowired
	private SubtitlesDao subtitlesDao;

	@Autowired
	private TorrentDao torrentDao;

	@Autowired
	protected UserTorrentDao userTorrentDao;

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
		downloadFullSchedule(show);
	}


	public static String normalize(String name) {
		name = name.toLowerCase();
		name = name.replaceAll("['\"\\-]", "");
		name = name.replaceAll("[:&\\._\\+,\\(\\)!\\?/]", " ");
		name = name.replaceAll("and", " ");
		name = name.replaceAll("\\s+", " ");
		name = name.trim();
		return name;
	}

	// not normalizing: and, &
	public static String normalizeFoQueryString(String name) {
		name = name.toLowerCase();
		name = name.replaceAll("['\"\\-]", "");
		name = name.replaceAll("[:\\._\\+,\\(\\)!\\?/]", " ");
		name = name.replaceAll("\\s+", " ");
		name = name.trim();
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
									if (show.isEnded() != downloadedShow.isEnded()) {
										show.setEnded(downloadedShow.isEnded());
										showsCacheService.updateShowEnded(show);
										// since show becomes ended, download its episodes schedule one last time
										downloadFullSchedule(show);
									}
								}
							}
						});
					} catch (Exception e) {
						if (Utils.isRootCauseMessageContains(e, "Read timed out")) {
							logService.warn(aClass, "Failed downloading info for show '" + downloadedShow.getName() + "' (Connection error)");
						} else {
							logService.error(aClass, "Failed downloading info for show '" + downloadedShow.getName() + "': " + e.getMessage(), e);
						}
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
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void downloadFullScheduleWithTorrents(final Show nonTransactionShow, boolean torrentsDownloadAsync) {
		// must separate schedule download and torrent download into separate transactions
		// cuz in the first creating episodes which must be available (committed) in the second part
		// and the second part spawns separate threads and transactions
		final DownloadScheduleResult downloadScheduleResult = transactionTemplate.execute(new TransactionCallback<DownloadScheduleResult>() {
			@Override
			public DownloadScheduleResult doInTransaction(TransactionStatus arg0) {
				Show show = showDao.find(nonTransactionShow.getId());
				return downloadFullSchedule(show);
			}
		});

		if (torrentsDownloadAsync) {
			final Class aClass = getClass();
			Executors.newSingleThreadExecutor().submit(new Runnable() {
				@Override
				public void run() {
					try {
						downloadScheduleHelper(downloadScheduleResult);
					} catch (Exception e) {
						logService.error(aClass, "Failed downloading schedule of show \"" + nonTransactionShow + "\" " + e.getMessage(), e);
					}
				}
			});
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public DownloadScheduleResult downloadFullSchedule(final Show show) {
		logService.info(getClass(), "Downloading full schedule for '" + show + "'");
		// need to re-query so it will be in this transaction
		Collection<Episode> episodes = showsProvider.downloadSchedule(show);
		DownloadScheduleResult downloadScheduleResult = new DownloadScheduleResult();
		downloadScheduleResultHelper(show, episodes, downloadScheduleResult);
		show.setScheduleDownloadDate(new Date());
		showsCacheService.updateShowEnded(show);
		return downloadScheduleResult;
	}

	private void downloadScheduleHelper(DownloadScheduleResult downloadScheduleResult) {
		// download torrents for the new episodes
		final Set<ShowRequest> episodesToDownload = new HashSet<>();
		for (Episode episode : downloadScheduleResult.getNewEpisodes()) {
			Show show = episode.getShow();
			if (episode.getEpisode() == -1) {
				episodesToDownload.add(new FullSeasonRequest(show.getName(), show, MediaQuality.HD720P, episode.getSeason()));
			} else {
				episodesToDownload.add(new SingleEpisodeRequest(show.getName(), show, MediaQuality.HD720P, episode.getSeason(), episode.getEpisode()));
			}
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
	public DownloadScheduleResult downloadLatestScheduleWithTorrents() {
		// must separate schedule download and torrent download into separate transactions
		// cuz in the first creating episodes which must be available (committed) in the second part
		// and the second part spawns separate threads and transactions
		final DownloadScheduleResult downloadScheduleResult = transactionTemplate.execute(new TransactionCallback<DownloadScheduleResult>() {
			@Override
			public DownloadScheduleResult doInTransaction(TransactionStatus arg0) {
				DownloadScheduleResult downloadScheduleResult = new DownloadScheduleResult();

				Collection<Episode> episodes = showsProvider.downloadSchedule();

				// collect all future episode schedules as given by the showsProvider
				Map<Show, List<Episode>> map = new HashMap<>();

				// just to skip already processed shows
				Map<Show, Boolean> processedShows = new HashMap<>();
				Map<Show, Show> showShellToShowMap = new HashMap<>();
				for (Episode episode : episodes) {
					Show showShell = episode.getShow();
					if (!processedShows.containsKey(showShell)) {
						Show show = showDao.findByTvRageId(showShell.getTvRageId());
						if (show == null) {
							show = showDao.findByName(showShell.getName());
						}

						// don't save new shows in the map, only save the show to db
						// if show is not found, it is not being tracked
						if (show == null) {
							saveNewShow(showShell);
							processedShows.put(showShell, false);
						} else {
							if (show.getTvRageId() == -1) {
								show.setTvRageId(showShell.getTvRageId());
							}
							processedShows.put(showShell, userDao.isShowBeingTracked(show));
							showShellToShowMap.put(showShell, show);
						}
					}

					// save only tracked shows in the map (also here show != null)
					if (processedShows.get(showShell)) {
						Show show = showShellToShowMap.get(showShell);
						CollectionUtils.safeListPut(map, show, episode);
						episode.setShow(show); // replacing showShell with real show
					}
				}

				// handle the case, where the job didn't run for a long time, and there is a gap between
				// the schedules and what we have in db - we need to fill this gap by downloading full show schedule
				// for the tracked shows
				if (shouldDownloadGap(map, episodes)) {
					logService.info(getClass(), "Detected that there is an episode schedule gap, " +
												"will download full schedules for tracked shows");
					for (Show show : map.keySet()) {
						downloadFullSchedule(show);
					}
				} else {
					for (Map.Entry<Show, List<Episode>> entry : map.entrySet()) {
						Show show = entry.getKey();
						List<Episode> curEpisodes = entry.getValue();

						try {
							logService.info(getClass(), "Downloading latest schedule for '" + show + "'");
							downloadScheduleResultHelper(show, curEpisodes, downloadScheduleResult);
						} catch (Exception e) {
							downloadScheduleResult.addFailedShow(show);
							// why before there was no exception trace printed to log?
							logService.error(getClass(), "Failed downloading schedule for show " + show + " " + e.getMessage(), e);
						}
					}
				}

				return downloadScheduleResult;
			}
		});

		downloadScheduleHelper(downloadScheduleResult);
		return downloadScheduleResult;
	}

	// checking if the earliest airdate episode is already in db
	private boolean shouldDownloadGap(Map<Show, List<Episode>> map, Collection<Episode> episodes) {
		boolean shouldDownloadGap = false;
		if (!episodes.isEmpty()) {
			List<Episode> sortedEpisodes = new ArrayList<>(episodes);
			Collections.sort(sortedEpisodes, new Comparator<Episode>() {
				@Override
				public int compare(Episode o1, Episode o2) {
					// air dates shouldn't be null here
					return o1.getAirDate().compareTo(o2.getAirDate());
				}
			});
			// look for an episode with existing show
			for (Episode episode : sortedEpisodes) {
				if (map.containsKey(episode.getShow())) {
					shouldDownloadGap = !episodeDao.exists(episode.getShow(), episode);
					break;
				}
			}
		}
		return shouldDownloadGap;
	}

	private void downloadScheduleResultHelper(Show show, Collection<Episode> episodes, DownloadScheduleResult downloadScheduleResult) {
		EpisodesMapper mapper = new EpisodesMapper(show.getEpisodes());
		for (Episode episode : episodes) {
			Episode persistedEpisode = mapper.get(episode.getSeason(), episode.getEpisode());
			if (persistedEpisode == null) {
				persistEpisodeToShow(show, episode);
				downloadScheduleResult.addNewEpisode(episode);
				persistedEpisode = episode;
			}
			persistedEpisode.setAirDate(episode.getAirDate());
		}

		// create full season episode if needed. Take all episodes, sort them and look at the last one
		// all season before the season of the last episode should exist
		// persisting inside
		for (Episode episode : findMissingFullSeasonEpisodes(show)) {
			downloadScheduleResult.addNewEpisode(episode);
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
	public Collection<Episode> findMissingFullSeasonEpisodes(Show show) {
		Collection<Episode> fullSeasonNewEpisodes = new ArrayList<>();
		if (!show.getEpisodes().isEmpty()) {
			EpisodesMapper mapper = new EpisodesMapper(show.getEpisodes());
			ArrayList<Episode> sortedEpisodes = new ArrayList<>(show.getEpisodes());
			Collections.sort(sortedEpisodes, new EpisodesComparator());
			int lastEpisodeSeason = sortedEpisodes.get(sortedEpisodes.size() - 1).getSeason();
			// running until lastEpisodeSeason-1 (including)
			for (int i = 1; i < lastEpisodeSeason; ++i) {
				if (mapper.get(i, -1) == null) {
					Episode episode = new Episode(i, -1);
					persistEpisodeToShow(show, episode);
					fullSeasonNewEpisodes.add(episode);
				}
			}
		}
		return fullSeasonNewEpisodes;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void disconnectTorrentsFromEpisode(Episode episode) {
		// episode is always connected to a single show - not a problem
		for (Long torrentId : episode.getTorrentIds()) {
			// delete the torrent only if it is connected to a single episode
			Torrent torrent = torrentDao.find(torrentId);
			try {
				episodeDao.find(torrent);

				for (UserTorrent userTorrent : userTorrentDao.findUserEpisodeTorrentByTorrentId(torrentId)) {
					userTorrentDao.delete(userTorrent);
				}

				for (Subtitles subtitles : subtitlesDao.findByTorrent(torrent)) {
					subtitlesDao.delete(subtitles);
				}

				torrentDao.delete(torrent);
			} catch (NonUniqueResultException e) {
//				System.out.println("Aaaaa " + e.getMessage());
//				e.printStackTrace();
				// if exception was thrown - it means the torrent is connected to multiple episodes
			}
		}

		episode.getTorrentIds().clear();
		episodeDao.merge(episode);
	}

	@Override
	public void persistEpisodeToShow(Show show, Episode episode) {
		episodeDao.persist(episode);
		episode.setShow(show);
		show.getEpisodes().add(episode);
	}

	@Override
	public Set<MatchCandidate> filterMatching(EpisodeRequest mediaRequest, Collection<MatchCandidate> movieRequests) {
		if (movieRequests.isEmpty()) {
			return Collections.emptySet();
		}

		// take everything before s01e01
		// do LD on the texts
		String requestTitle = ShowServiceImpl.normalize(mediaRequest.getTitle());
		String seasonEpisode = mediaRequest.getSeasonEpisode();
		Show show = mediaRequest.getShow();

		List<Pair<Integer, MatchCandidate>> pairs = new ArrayList<>();
		for (MatchCandidate movieRequest : movieRequests) {
			String title = ShowServiceImpl.normalize(movieRequest.getText());

			String titlePrefix = null;
			String titleSuffix = null;

			if (mediaRequest instanceof FullSeasonRequest) {
				// take everything before season 1 or s01 the first of the 2
				String fullSeasonEnum = "season " + mediaRequest.getSeason();
				String shortSeasonEnum = "s" + StringUtils.leftPad(String.valueOf(mediaRequest.getSeason()), 2, '0');

				int indexOfFullSeason = title.indexOf(fullSeasonEnum);
				int indexOfShortSeason = title.indexOf(shortSeasonEnum);
				if (indexOfFullSeason == -1) {
					if (indexOfShortSeason != -1) {
						titlePrefix = title.substring(0, indexOfShortSeason).trim();
						titleSuffix = title.substring(indexOfShortSeason + shortSeasonEnum.length());
					}
				} else if (indexOfShortSeason == -1) {
					titlePrefix = title.substring(0, indexOfFullSeason);
					titleSuffix = title.substring(indexOfFullSeason + fullSeasonEnum.length());
				} else if (indexOfFullSeason < indexOfShortSeason) {
					titlePrefix = title.substring(0, indexOfFullSeason);
					titleSuffix = title.substring(indexOfFullSeason + fullSeasonEnum.length());
				} else {
					titlePrefix = title.substring(0, indexOfShortSeason);
					titleSuffix = title.substring(indexOfShortSeason + shortSeasonEnum.length());
				}
			} else {
				int indexOfSeasonEpisode = title.indexOf(seasonEpisode);
				if (indexOfSeasonEpisode != -1) {
					// take everything before s01e01
					titlePrefix = title.substring(0, indexOfSeasonEpisode);
					titleSuffix = title.substring(indexOfSeasonEpisode + seasonEpisode.length());
				}
			}

			// if we were in any of the conditions
			if (titlePrefix != null) {
				// show name is a heavier search than the title suffix one
				titlePrefix = titlePrefix .trim();
				boolean titleSuffixMatch = isTitleSuffixMatch(titleSuffix);
				if (titleSuffixMatch && isShowNameMatch(titlePrefix, show)) {
					pairs.add(new ImmutablePair<>(StringUtils.getLevenshteinDistance(titlePrefix, requestTitle), movieRequest));
				} else {
					logService.info(getClass(), "Removing '" + title + "' cuz a bad " + (titleSuffixMatch ? "show name" : "title suffix") + " match for '" + mediaRequest.toString() + "'");
				}
			}
		}

		Set<MatchCandidate> result = new HashSet<>();
		if (pairs.isEmpty()) {
			return result;
		}

		Collections.sort(pairs, new Comparator<Pair<Integer, MatchCandidate>>() {
			@Override
			public int compare(Pair<Integer, MatchCandidate> o1, Pair<Integer, MatchCandidate> o2) {
				return Ints.compare(o1.getKey(), o2.getKey());
			}
		});
		Pair<Integer, MatchCandidate> first = pairs.get(0);
		int i;
		for (i = 1; i < pairs.size(); ++i) {
			if (!first.getKey().equals(pairs.get(i).getKey())) {
				break;
			}
		}

		// log skipped entries
		for (Pair<Integer, MatchCandidate> pair : pairs.subList(i, pairs.size())) {
			logService.info(getClass(), "Removing '" + pair.getValue().getText() + "' cuz a bad match for '" + mediaRequest.toString() + "'");
		}

		for (Pair<Integer, MatchCandidate> pair : pairs.subList(0, i)) {
			result.add(pair.getValue());
		}
		return result;
	}

	private boolean isShowNameMatch(String title, Show show) {
		for (Show curShow : showSearchService.statisticMatch(title)) {
			if (curShow.equals(show)) {
				return true;
			}
		}
		return false;
	}

	private boolean isTitleSuffixMatch(String titleSuffix) {
		titleSuffix = titleSuffix.trim();
		// take only when after season 1 there is text or number > 100 or  nothing
		if (StringUtils.isBlank(titleSuffix) || Character.isLetter(titleSuffix.charAt(0))) {
			return true;
		}

		if (titleSuffix.length() >= 3) {
			String t = titleSuffix.substring(0, 3);
			if (NumberUtils.isNumber(t) && Integer.parseInt(t) >= 100) {
				return true;
			}
		}
		return false;
	}
}
