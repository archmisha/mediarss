package rss.services.shows;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.EpisodesComparator;
import rss.controllers.EntityConverter;
import rss.controllers.vo.EpisodeSearchResult;
import rss.controllers.vo.UserTorrentVO;
import rss.dao.ShowDao;
import rss.dao.TorrentDao;
import rss.dao.UserDao;
import rss.dao.UserTorrentDao;
import rss.entities.*;
import rss.services.downloader.TVShowsTorrentEntriesDownloader;
import rss.services.log.LogService;
import rss.services.requests.ShowRequest;
import rss.util.DurationMeter;
import rss.util.MultiThreadExecutor;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: dikmanm
 * Date: 16/04/13 17:14
 */
@Service
public class ShowSearchServiceImpl implements ShowSearchService {

	private static final int MAX_DID_YOU_MEAN = 20;

	@Autowired
	private TorrentDao torrentDao;

	@Autowired
	private UserTorrentDao userTorrentDao;

	@Autowired
	private EntityConverter entityConverter;

	@Autowired
	private TVShowsTorrentEntriesDownloader torrentEntriesDownloader;

	@Autowired
	private ShowService showService;

	@Autowired
	private LogService logService;

	@Autowired
	private ShowDao showDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private ShowsCacheService showsCacheService;

	@Override
	public EpisodeSearchResult search(ShowRequest episodeRequest, User user, boolean forceDownload) {
		// saving original search term - it might change during the search
		String originalSearchTerm = episodeRequest.getTitle();
		String actualSearchTerm;

		DurationMeter duration = new DurationMeter();
		Collection<Show> didYouMeanShows = statisticMatch(originalSearchTerm, MAX_DID_YOU_MEAN);
		duration.stop();
		logService.info(getClass(), "Did you mean time - " + duration.getDuration() + " millis");

		// first check which show we need
		Show show = findShowByName(originalSearchTerm);
		if (show != null) {
			episodeRequest.setShow(show);
			episodeRequest.setTitle(show.getName());
			actualSearchTerm = show.getName();
			if (originalSearchTerm.toLowerCase().equals(actualSearchTerm.toLowerCase())) {
				originalSearchTerm = actualSearchTerm;
			}
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

		downloadShowScheduleBeforeSearch(episodeRequest.getShow());

		Collection<Episode> downloaded = torrentEntriesDownloader.download(Collections.singleton(episodeRequest), forceDownload).getDownloaded();

		Map<Torrent, Episode> episodeByTorrents = new HashMap<>();
		final Map<Long, Episode> episodeByTorrentsForComparator = new HashMap<>();
		for (Episode episode : downloaded) {
			for (Long torrentId : episode.getTorrentIds()) {
				Torrent torrent = torrentDao.find(torrentId);
				episodeByTorrents.put(torrent, episode);
				episodeByTorrentsForComparator.put(torrent.getId(), episode);
			}
		}

		ArrayList<UserTorrentVO> result = new ArrayList<>();

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


		return EpisodeSearchResult.createWithResult(originalSearchTerm, actualSearchTerm, result, entityConverter.toThinShows(didYouMeanShows));
	}

	private Show findShowByName(String name) {
		String normalizedName = ShowServiceImpl.normalize(name);
		for (CachedShow cachedShow : showsCacheService.getAll()) {
			if (cachedShow.getNormalizedName().equals(normalizedName)) {
				return showDao.find(cachedShow.getId());
			}
		}
		return null;
	}

	private void downloadShowScheduleBeforeSearch(Show show) {
		// download show episode schedule unless:
		// - already downloaded schedule and show is ended
		// - show is being tracked
		// - the last episode we have is aired after now
		boolean shouldDownloadSchedule = true;
		if (show.isEnded()) {
			if (show.getScheduleDownloadDate() != null) {
				shouldDownloadSchedule = false;
			}
		} else if (userDao.isShowBeingTracked(show)) {
			shouldDownloadSchedule = false;
		} else {
			ArrayList<Episode> episodes = new ArrayList<>(show.getEpisodes());
			if (episodes.isEmpty()) {
				Collections.sort(episodes, new EpisodesComparator());
				Episode lastEpisode = episodes.get(episodes.size() - 1);
				if (lastEpisode.getAirDate() != null && lastEpisode.getAirDate().after(new Date())) {
					shouldDownloadSchedule = false;
				}
			}
		}
		if (shouldDownloadSchedule) {
			showService.downloadFullSchedule(show);
		}
	}

	public Collection<Show> statisticMatch(String name) {
		return statisticMatch(name, Integer.MAX_VALUE);
	}

	// Levenshtein distance (LD)
	// Don't use threshold, cuz maybe our name is shorter than the actual name... like spartacus: ....
	// and we search simply for spartacus
	private Collection<Show> statisticMatch(String name, int maxResults) {
		name = ShowServiceImpl.normalize(name);
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
		if (matches.size() > maxResults) {
			Collections.sort(matchesList, new Comparator<CachedShow>() {
				@Override
				public int compare(CachedShow o1, CachedShow o2) {
					return Integer.valueOf(o1.getWords()).compareTo(o2.getWords());
				}
			});
		}
		Collection<Show> result = new ArrayList<>();
		for (CachedShow match : matchesList.subList(0, Math.min(matchesList.size(), maxResults))) {
			result.add(showDao.find(match.getId()));
		}

		logService.debug(getClass(), "Show statistic match end for: " + name + " found: " + StringUtils.join(result.toArray(), ","));
		return result;
	}
}
