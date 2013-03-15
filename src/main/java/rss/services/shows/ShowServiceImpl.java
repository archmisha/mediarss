package rss.services.shows;

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

	@PostConstruct
	private void postConstruct() {
		showsProvider = new TVRageServiceImpl();
		showsProvider.setLogService(logService);
		showsProvider.setPageDownloader(pageDownloader);
	}

	private void saveNewShow(Show show) {
		logService.info(getClass(), "It is a new show! - Persisting '" + show.getName() + "' (tvrage_id=" + show.getTvRageId() + ")");
		showDao.persist(show);
		Collection<Episode> episodes = showsProvider.downloadSchedule(show);
		for (Episode episode : episodes) {
			episodeDao.persist(episode);
			episode.setShow(show);
			show.getEpisodes().add(episode);
		}
	}

	// Levenshtein distance
	// allow minimal and minimal+1
	public Collection<Show> statisticMatch(String name) {
		name = normalize(name);

		Set<Show> matches = new HashSet<>();
		Set<Show> containmentMatches = new HashSet<>();
		Collection<Show> shows = showDao.findAll();

		// first test for full containment - for case of search 'anatomy' we want 'grey's anatomy' result
		// and not arrow which has lower LD
		for (Show show : shows) {
			String cur = normalize(show.getName());
			if (cur.contains(name)) {
				containmentMatches.add(show);
			}
		}

		if (containmentMatches.size() == 1) {
			logService.info(getClass(), "found containing match:" + containmentMatches.iterator().next().getName());
			return containmentMatches;
		}

		// if there were several matches on the previous step - not continuing with them to LD like was the initial idea
		// better return them all as whatDidYouMean cuz spartacus there are several shows and don't want to choose for the user
		// now also adding a threshold for that, if there are under X shows then return them. otherwise it might be
		// just a stupid word like 'the' that is contained in every show
		if (!containmentMatches.isEmpty() && containmentMatches.size() <= 10) {
			logService.info(getClass(), "found several containing matches:" + StringUtils.join(containmentMatches.toArray(), ","));
			return containmentMatches;
		}

		// LD.
		// Don't use threshold, cuz maybe our name is shorter than the actual name... like spartacus: ....
		// and we search simply for spartacus
		int bestLD = Integer.MAX_VALUE;
		for (Show show : shows) {
			String cur = normalize(show.getName());
			int ld = StringUtils.getLevenshteinDistance(name, cur);

			List<String> nameSplit = Arrays.asList(name.split(" "));
			Collections.sort(nameSplit);
			String nameSorted = StringUtils.join(nameSplit.toArray(), " ");

			List<String> curSplit = Arrays.asList(cur.split(" "));
			Collections.sort(curSplit);
			String curSorted = StringUtils.join(curSplit.toArray(), " ");

			int ldSortedWords = StringUtils.getLevenshteinDistance(nameSorted, curSorted);

			if (ld == -1 && ldSortedWords == -1) {
				continue;
			}
			if (ld == -1) {
				ld = Integer.MAX_VALUE;
			}
			if (ldSortedWords == -1) {
				ldSortedWords = Integer.MAX_VALUE;
			}
			ld = Math.min(ld, ldSortedWords);

			if (matches.isEmpty() || ld == bestLD) {
				matches.add(show);
				logService.info(getClass(), "show=" + show.getName() + " ld=" + ld);
			} else if (ld < bestLD) {
				matches.clear();
				matches.add(show);
				bestLD = ld;
				logService.info(getClass(), "show=" + show.getName() + " ld=" + ld);
			}
			// optimizing average case
			bestLD = Math.min(bestLD, ld);
		}

		// if bestLD is the size of the input, it means need to transform all the chars - means its a bad match
		// also if there are only 2 letters match its a weak match
		if (matches.isEmpty() || Math.abs(name.length() - bestLD) <= 1) {
			logService.info(getClass(), "Show statistic match end for: " + name + " NOT FOUND");
			logService.info(getClass(), "Retuning " + containmentMatches.size() + " containment matches");
			return containmentMatches;
//			return Collections.emptyList();
		}

		logService.info(getClass(), "Show statistic match end for: " + name + " found: " + StringUtils.join(matches.toArray(), ","));
		return matches;
	}

	private String normalize(String name) {
		name = name.toLowerCase();
		name = name.replaceAll("'", "");
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

		// first check which show we need
		String actualSearchTerm;
		Collection<Show> didYouMeanShows = statisticMatch(originalSearchTerm);
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
	@Transactional(propagation = Propagation.REQUIRED)
	public void downloadSchedule(Show show) {
		Collection<Episode> episodes = showsProvider.downloadSchedule(show);
		DownloadScheduleResult downloadScheduleResult = new DownloadScheduleResult();
		downloadScheduleHelper(show, episodes, downloadScheduleResult);
	}

	@Override
	public List<Show> autoCompleteShowNames(String term) {
		return showDao.autoCompleteShowNames(term);
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
