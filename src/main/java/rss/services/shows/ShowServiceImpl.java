package rss.services.shows;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.EpisodesComparator;
import rss.controllers.EntityConverter;
import rss.controllers.vo.EpisodeSearchResult;
import rss.controllers.vo.ShowScheduleEpisodeItem;
import rss.controllers.vo.ShowsScheduleVO;
import rss.controllers.vo.UserTorrentVO;
import rss.dao.EpisodeDao;
import rss.dao.ShowDao;
import rss.dao.TorrentDao;
import rss.dao.UserTorrentDao;
import rss.entities.*;
import rss.services.EpisodeRequest;
import rss.services.PageDownloader;
import rss.services.SettingsService;
import rss.services.downloader.TVShowsTorrentEntriesDownloader;
import rss.services.log.LogService;
import rss.util.CollectionUtils;
import rss.util.DateUtils;
import rss.util.DurationMeter;

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
		Collections.sort(sortedNameSplit);
		String sortedNameJoined = StringUtils.join(sortedNameSplit.toArray(), " ");

		Set<CachedShow> matches = new HashSet<>();

		int bestLD = Integer.MAX_VALUE;
		for (Map.Entry<CachedShow, Collection<String>> entry : showsCacheService.getShowsSubsets()) {
			CachedShow show = entry.getKey();
			int ld = Integer.MAX_VALUE;
			for (String subset : entry.getValue()) {
				if (subset.contains(sortedNameJoined)) {
					ld = 0;
				} else {
					int curLd = StringUtils.getLevenshteinDistance(sortedNameJoined, subset);
					if (curLd != -1) {
						ld = Math.min(curLd, ld);
					}
				}
			}

			if (ld < bestLD) {
				matches.clear();
			}

			// if ld same as bestLD still want to add the show
			if (matches.isEmpty() || ld <= bestLD) {
				matches.add(show);
				bestLD = ld;
				logService.debug(getClass(), "show=" + show.getName() + " ld=" + ld);
			}
		}

		Collection<Show> result = new ArrayList<>();
		for (CachedShow match : matches) {
			result.add(showDao.find(match.getId()));
		}

		logService.debug(getClass(), "Show statistic match end for: " + name + " found: " + StringUtils.join(result.toArray(), ","));
		return result;
	}

	public static String normalize(String name) {
		name = name.toLowerCase();
		name = name.replaceAll("['\"!,\\.&\\-\\+\\?/:]", "");
		return name;
	}

	@Override
	// substitute show name with alias and seasons if needed
	public void transformEpisodeRequest(EpisodeRequest episodeRequest) {
		String alias = settingsService.getShowAlias(episodeRequest.getShow().getName());
		if (!StringUtils.isBlank(alias)) {
			episodeRequest.setTitle(alias);

			episodeRequest.setSeason(settingsService.getShowSeasonAlias(episodeRequest.getShow().getName(), episodeRequest.getSeason()));
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
	public EpisodeSearchResult search(EpisodeRequest episodeRequest, User user) {
		// saving original search term - it might change during the search
		String originalSearchTerm = episodeRequest.getTitle();
		DurationMeter duration = new DurationMeter();
		// first check which show we need
		String actualSearchTerm;
		Collection<Show> didYouMeanShows = statisticMatch(originalSearchTerm);
		duration.stop();
		DurationMeter duration2 = new DurationMeter();
		Show show = showDao.findByName(originalSearchTerm);

		duration2.stop();
		logService.info(getClass(), "AAAAAAAA - " + duration.getDuration() + "<>" + duration2.getDuration());

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
	@Transactional(propagation = Propagation.REQUIRED)
	public void downloadSchedule(Show show) {
		Collection<Episode> episodes = showsProvider.downloadSchedule(show);
		DownloadScheduleResult downloadScheduleResult = new DownloadScheduleResult();
		downloadScheduleHelper(show, episodes, downloadScheduleResult);
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
	@Transactional(propagation = Propagation.REQUIRED)
	public DownloadScheduleResult downloadSchedule() {
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

			logService.info(getClass(), "Downloading schedule for " + showShell);
			try {
				Show show = showDao.findByName(showShell.getName());
				if (show == null) {
					saveNewShow(showShell);
					show = showShell;
					downloadScheduleResult.addNewEpisodes(curEpisodes);
				} else if (show.getTvRageId() == -1) {
					show.setTvRageId(showShell.getTvRageId());
				}

				downloadScheduleHelper(show, curEpisodes, downloadScheduleResult);
			} catch (Exception e) {
				downloadScheduleResult.addFailedShow(showShell);
				logService.error(getClass(), "Failed downloading schedule for show " + showShell);
			}
		}

		// in addition we need past episodes, which might have no torrents for some reason (maybe job was broken or something)
		for (Show show : showDao.findNotEnded()) {
			for (Episode episode : show.getEpisodes()) {
				if (episode.getTorrentIds().isEmpty()) {
					// if there are episodes without torrents, try to find their torrents
					downloadScheduleResult.addNewEpisode(episode);
				}
			}
		}

		return downloadScheduleResult;
	}

	private void downloadScheduleHelper(Show show, Collection<Episode> episodes, DownloadScheduleResult downloadScheduleResult) {
		EpisodesMapper mapper = new EpisodesMapper(show.getEpisodes());
		for (Episode episode : episodes) {
			Episode persistedEpisode = mapper.get(episode.getSeason(), episode.getEpisode());
			if (persistedEpisode == null) {
				episodeDao.persist(episode);
				episode.setShow(show);
				show.getEpisodes().add(episode);
				downloadScheduleResult.addNewEpisode(episode);
				persistedEpisode = episode;
			} else if (persistedEpisode.getTorrentIds().isEmpty()) {
				// if there are episodes without torrents, try to find their torrents
				downloadScheduleResult.addNewEpisode(persistedEpisode);
			}
			persistedEpisode.setAirDate(episode.getAirDate());
		}
	}

	@Override
	public boolean isMatch(EpisodeRequest mediaRequest, String title) {
		String requestTitle = ShowServiceImpl.normalize(mediaRequest.getTitle()).toLowerCase();
		String seasonEpisode = mediaRequest.getSeasonEpisode();
		title = ShowServiceImpl.normalize(title.toLowerCase());
		return title.contains(requestTitle) && title.contains(seasonEpisode);
	}

	/*@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Show downloadShowByUrl(String url) {
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		if (!url.endsWith("episodes")) {
			url += "/episodes";
		}

		Show show = showsProvider.downloadShowByUrl(url);
		Show persistedShow = showDao.findByName(show.getName());
		if (persistedShow == null) {
			saveNewShow(show);
			persistedShow = show;
		}
		return persistedShow;
	}*/
}
