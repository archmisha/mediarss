package rss.services.downloader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.EpisodesComparator;
import rss.MediaRSSException;
import rss.ShowNotFoundException;
import rss.dao.EpisodeDao;
import rss.dao.ShowDao;
import rss.dao.TorrentDao;
import rss.entities.Episode;
import rss.entities.MediaQuality;
import rss.entities.Show;
import rss.entities.Torrent;
import rss.services.SearchResult;
import rss.services.SubtitlesService;
import rss.services.requests.*;
import rss.services.searchers.TorrentSearcher;
import rss.services.shows.EpisodesMapper;
import rss.services.shows.ShowService;
import rss.util.CollectionUtils;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * User: Michael Dikman
 * Date: 02/12/12
 * Time: 23:49
 */
@Service("tVShowsTorrentEntriesDownloader")
public class TVShowsTorrentEntriesDownloader extends TorrentEntriesDownloader<Episode, ShowRequest> {

	@Autowired
	private EpisodeDao episodeDao;

	@Autowired
	private TorrentDao torrentDao;

	@Autowired
	@Qualifier("smartEpisodeSearcher")
	private TorrentSearcher<ShowRequest, Episode> smartEpisodeSearcher;

	@Autowired
	private ShowService showService;

	@Autowired
	private SubtitlesService subtitlesService;

	@Autowired
	private ShowDao showDao;

	@Override
	protected SearchResult<Episode> downloadTorrent(ShowRequest episodeRequest) {
		return smartEpisodeSearcher.search(episodeRequest);
	}

	@Override
	protected void processMissingRequests(Collection<ShowRequest> missing) {
		for (Episode episode : episodeDao.find(missing)) {
			episode.setScanDate(new Date());
		}
	}

	@Override
	protected boolean validateSearchResult(ShowRequest mediaRequest, SearchResult<Episode> searchResult) {
		return true;
	}

	@Override
	protected List<Episode> processSearchResults(Collection<Pair<ShowRequest, SearchResult<Episode>>> results) {
		List<Episode> res = new ArrayList<>();
		for (Pair<ShowRequest, SearchResult<Episode>> pair : results) {
			SearchResult<Episode> searchResult = pair.getValue();
			ShowRequest showRequest = pair.getKey();

//		Show persistedShow = showDao.find(episodeRequest.getShow().getId());
			Torrent torrent = searchResult.getTorrent();

			List<Episode> persistedEpisodes = episodeDao.find((EpisodeRequest) showRequest);

			// sometimes the same torrent returned from search for different episodes
			// it can happen when there are torrents like s01e01-e04 will be returned for s01e(-1) request also
			Torrent persistedTorrent = torrentDao.findByUrl(torrent.getUrl());
			if (persistedTorrent == null) {
				torrentDao.persist(torrent);
				persistedTorrent = torrent;

				// handle subtitles - cant try-catch here, cuz it tried to insert the new entities first and if failed we
				// don't know and un-persisted episode with torrent null is returned! if want try catch need in separate transaction or something
				// download subtitles only if enabled for this show for any user
//			for (Episode persistedEpisode : persistedEpisodes) {
//				List<SubtitleLanguage> subtitlesLanguages = episodeDao.getSubtitlesLanguages(persistedEpisode);
//				if (!subtitlesLanguages.isEmpty()) {
//					subtitlesService.downloadEpisodeSubtitles(torrent, persistedEpisode, subtitlesLanguages);
//				}
//			}
			}

			for (Episode persistedEpisode : persistedEpisodes) {
				persistedEpisode.getTorrentIds().add(persistedTorrent.getId());
				persistedEpisode.setScanDate(new Date());
			}

			res.addAll(persistedEpisodes);
		}
		return res;
	}

	@Override
	// persisting new shows here - must be persisted before the new transaction opens for each download
	protected Collection<Episode> preDownloadPhase(Set<ShowRequest> episodeRequests, boolean forceDownload) {
		// substitute show name with alias and seasons if needed
		for (ShowRequest episodeRequest : new HashSet<>(episodeRequests)) {
			showService.transformEpisodeRequest(episodeRequest);
		}

		for (ShowRequest episodeRequest : episodeRequests) {
			if (episodeRequest.getShow() == null) {
				throw new ShowNotFoundException("Show not found: " + episodeRequest.getTitle());
			}
		}

		Map<String, Map<Integer, Pair<TreeSet<Episode>, Boolean>>> eps = createEpisodesMapFromDB(episodeRequests);

		// must remove FullShowRequest before building the episodes to requests map
		expandFullShowRequests(episodeRequests, eps);
		expandFullSeasonRequests(episodeRequests, eps);

		Map<Episode, Set<EpisodeRequest>> episodesMap = createEpisodesToRequestsMap(episodeRequests, eps, forceDownload);

		// the only place that persists full season Episode objects
		addFullSeasonEpisodes(episodeRequests, episodesMap);
		skipUnAiredEpisodes(episodeRequests, episodesMap);

		// doesn't add new episodes, only requests on existing episodes - updates episodesMap
		handleDoubleEpisodes(episodeRequests, eps, episodesMap);

		Set<Episode> cachedEpisodes;
		if (forceDownload) {
			cachedEpisodes = Collections.emptySet();
		} else {
			cachedEpisodes = skipCachedEpisodes(episodeRequests, episodesMap);
			skipScannedEpisodes(episodeRequests, eps, episodesMap);
		}

		return cachedEpisodes;
	}

	private Set<Episode> skipCachedEpisodes(Set<ShowRequest> episodeRequests, Map<Episode, Set<EpisodeRequest>> episodesMap) {
		Set<Episode> cachedEpisodes = new HashSet<>();
		for (Map.Entry<Episode, Set<EpisodeRequest>> entry : new ArrayList<>(episodesMap.entrySet())) {
			Episode episode = entry.getKey();
			if (!episode.getTorrentIds().isEmpty()) {
				logService.debug(this.getClass(), "Episode \"" + episode + "\" was found in cache");
				cachedEpisodes.add(episode);
				episodeRequests.removeAll(entry.getValue());

				// removing to skip in the following iteration loop
				episodesMap.remove(episode);
			}
		}
		return cachedEpisodes;
	}

	private void addFullSeasonEpisodes(Set<ShowRequest> episodeRequests, Map<Episode, Set<EpisodeRequest>> episodesMap) {
		Set<Show> shows = new HashSet<>();
		for (Episode episode : episodesMap.keySet()) {
			shows.add(episode.getShow());
		}

		for (Show show : shows) {
			for (Episode episode : showService.findMissingFullSeasonEpisodes(show)) {
				// only if this full season episode was requested in the first place
				if (episodesMap.containsKey(episode)) {
					EpisodeRequest episodeRequest = new FullSeasonRequest(show.getName(), show, MediaQuality.HD720P, episode.getSeason());
					episodeRequests.add(episodeRequest);
					CollectionUtils.safeSetPut(episodesMap, episode, episodeRequest);
				}
			}
		}
	}

	// forceDownload is true then need to delete the previous torrents of the episode
	private Map<Episode, Set<EpisodeRequest>> createEpisodesToRequestsMap(Set<ShowRequest> episodeRequests,
																		  Map<String, Map<Integer, Pair<TreeSet<Episode>, Boolean>>> eps,
																		  boolean forceDownload) {
		Map<String, EpisodesMapper> eps2 = new HashMap<>();
		for (Map.Entry<String, Map<Integer, Pair<TreeSet<Episode>, Boolean>>> entry : eps.entrySet()) {
			String showName = entry.getKey();
			EpisodesMapper episodesMapper = new EpisodesMapper();
			for (Map.Entry<Integer, Pair<TreeSet<Episode>, Boolean>> entry2 : entry.getValue().entrySet()) {
				episodesMapper.add(entry2.getValue().getKey());
			}
			eps2.put(showName, episodesMapper);
		}

		Map<Episode, Set<EpisodeRequest>> episodesMap = new HashMap<>();
		for (ShowRequest showRequest : new HashSet<>(episodeRequests)) {
			EpisodeRequest episodeRequest = (EpisodeRequest) showRequest;
			if (episodeRequest instanceof SingleEpisodeRequest) {
				EpisodesMapper episodesMapper = eps2.get(episodeRequest.getShow().getName());
				Episode episode = episodesMapper.get(episodeRequest.getSeason(), ((SingleEpisodeRequest) episodeRequest).getEpisode());
				if (episode == null) {
					logService.info(getClass(), "Skipping request '" + episodeRequest + "' because no such episodes exist");
					episodeRequests.remove(episodeRequest);
				} else {
					// if not yet handled this episode
					if (forceDownload && !episodesMap.containsKey(episode)) {
						deleteTorrents(episode);
					}
					CollectionUtils.safeSetPut(episodesMap, episode, episodeRequest);
				}
			} else if (episodeRequest instanceof DoubleEpisodeRequest) {
				EpisodesMapper episodesMapper = eps2.get(episodeRequest.getShow().getName());
				Episode episode1 = episodesMapper.get(episodeRequest.getSeason(), ((DoubleEpisodeRequest) episodeRequest).getEpisode1());
				Episode episode2 = episodesMapper.get(episodeRequest.getSeason(), ((DoubleEpisodeRequest) episodeRequest).getEpisode2());
				if (episode1 == null || episode2 == null) {
					logService.info(getClass(), "Skipping request '" + episodeRequest + "' because no such episodes exist");
					episodeRequests.remove(episodeRequest);
				} else {
					// if not yet handled this episode
					if (forceDownload && !episodesMap.containsKey(episode1)) {
						deleteTorrents(episode1);
					}
					// if not yet handled this episode
					if (forceDownload && !episodesMap.containsKey(episode2)) {
						deleteTorrents(episode2);
					}
					CollectionUtils.safeSetPut(episodesMap, episode1, episodeRequest);
					CollectionUtils.safeSetPut(episodesMap, episode2, episodeRequest);
				}
			} else if (episodeRequest instanceof FullSeasonRequest) {
				Map<Integer, Pair<TreeSet<Episode>, Boolean>> map = eps.get(episodeRequest.getShow().getName());
				if (!map.containsKey(episodeRequest.getSeason())) {
					logService.info(getClass(), "Skipping request '" + episodeRequest + "' because no such episodes exist");
					episodeRequests.remove(episodeRequest);
				} else {
					for (Episode episode : map.get(episodeRequest.getSeason()).getKey()) {
						if (episode == null) {
							logService.info(getClass(), "Skipping request '" + episodeRequest + "' because no such episodes exist");
							episodeRequests.remove(episodeRequest);
							break;
						}
						// if not yet handled this episode
						if (forceDownload && !episodesMap.containsKey(episode)) {
							deleteTorrents(episode);
						}
						CollectionUtils.safeSetPut(episodesMap, episode, episodeRequest);
					}
				}
			} else {
				throw new InvalidParameterException("Unhandled class of Episode request: " + episodeRequest.getClass());
			}
		}
		return episodesMap;
	}

	private void deleteTorrents(Episode episode) {
		showService.disconnectTorrentsFromEpisode(episode);
	}

	// skip episodes that already scanned, except the last 2 (those might still be updated later)
	private void skipScannedEpisodes(Set<ShowRequest> episodeRequests,
									 Map<String, Map<Integer, Pair<TreeSet<Episode>, Boolean>>> eps,
									 Map<Episode, Set<EpisodeRequest>> episodesMap) {
		if (episodeRequests.isEmpty()) {
			return;
		}

		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -14);
		for (Map.Entry<Episode, Set<EpisodeRequest>> entry : new ArrayList<>(episodesMap.entrySet())) {
			Episode episode = entry.getKey();
			// skip if already scanned and
			// 1. episode airdate is older than 14 days ago (full season has air date null)
			// 2. if its a full season and its last episode aired 14 days ago (only finished seasons here, unaired seasons filtered before)
			if (episode.getScanDate() != null) {
				boolean shouldRemoveEpisode = false;
				if (episode.getEpisode() > 0 && episode.getAirDate() != null && episode.getAirDate().before(c.getTime())) {
					shouldRemoveEpisode = true;
				} else if (episode.getEpisode() == -1) {
					TreeSet<Episode> episodes = eps.get(episode.getShow().getName()).get(episode.getSeason()).getKey();
					// episode -1 is sorted at the beginning, so only need to check there is more than 1 episode in the list
					if (!episodes.isEmpty()) {
						Episode ep = episodes.last();
						if (ep.getEpisode() > 0) {
							if (episode.getAirDate() != null && episode.getAirDate().before(c.getTime())) {
								shouldRemoveEpisode = true;
							}
						}
					}
				}

				if (shouldRemoveEpisode) {
					Set<EpisodeRequest> episodeRequest = entry.getValue();
					episodeRequests.removeAll(episodeRequest);
					// removing to skip in the following iteration loop
					episodesMap.remove(episode);
					logService.debug(getClass(), "Skipping downloading '" + StringUtils.join(episodeRequest, ", ") + "' - already scanned and airdate is older than 14 days ago");
				}
			}
		}
	}

	private void skipUnAiredEpisodes(Set<ShowRequest> episodeRequests, Map<Episode, Set<EpisodeRequest>> episodesMap) {
		if (episodeRequests.isEmpty()) {
			return;
		}

		for (Map.Entry<Episode, Set<EpisodeRequest>> entry : new ArrayList<>(episodesMap.entrySet())) {
			Episode episode = entry.getKey();
			if (episode.isUnAired()) {
				Set<EpisodeRequest> episodeRequest = entry.getValue();
				episodeRequests.removeAll(episodeRequest);
				// removing to skip in the following iteration loop
				episodesMap.remove(episode);
				logService.debug(getClass(), "Skipping downloading: " + StringUtils.join(episodeRequest, ", ") + " - still un-aired");
			}
		}
	}

	private void handleDoubleEpisodes(Set<ShowRequest> episodeRequests,
									  Map<String, Map<Integer, Pair<TreeSet<Episode>, Boolean>>> eps,
									  Map<Episode, Set<EpisodeRequest>> episodesMap) {
		if (episodeRequests.isEmpty()) {
			return;
		}

		// add double episode requests
		MediaQuality quality = episodeRequests.iterator().next().getQuality();
		for (Map.Entry<String, Map<Integer, Pair<TreeSet<Episode>, Boolean>>> showEntry : eps.entrySet()) {
			String showName = showEntry.getKey();
			for (Map.Entry<Integer, Pair<TreeSet<Episode>, Boolean>> entry : showEntry.getValue().entrySet()) {
				Integer season = entry.getKey();
				TreeSet<Episode> episodes = entry.getValue().getKey();
				if (episodes.size() > 1) {
					Iterator<Episode> it = episodes.iterator();
					Episode ep1 = it.next();
					while (it.hasNext()) {
						Episode ep2 = it.next();
						// if one of the episodes was requested in the search, both released on the same day and one of them lacks torrents
						if ((episodesMap.containsKey(ep1) || episodesMap.containsKey(ep2)) &&
							ep1.getAirDate() != null && ep2.getAirDate() != null && ep1.getAirDate().equals(ep2.getAirDate()) &&
							(ep1.getTorrentIds().isEmpty() || ep2.getTorrentIds().isEmpty())) {
							DoubleEpisodeRequest doubleEpisodeRequest = new DoubleEpisodeRequest(showName, ep1.getShow(), quality, season,
									ep1.getEpisode(), ep2.getEpisode());
							logService.info(getClass(), "Adding a double episode request: " + doubleEpisodeRequest.toString());
							episodeRequests.add(doubleEpisodeRequest);
							CollectionUtils.safeSetPut(episodesMap, ep1, doubleEpisodeRequest);
							CollectionUtils.safeSetPut(episodesMap, ep2, doubleEpisodeRequest);
						}

						ep1 = ep2;
					}
				}
			}
		}
	}

	private Map<String, Map<Integer, Pair<TreeSet<Episode>, Boolean>>> createEpisodesMapFromDB(Set<ShowRequest> episodeRequests) {
		Map<String, Map<Integer, Pair<TreeSet<Episode>, Boolean>>> eps = new HashMap<>();
		for (ShowRequest episodeRequest : new HashSet<>(episodeRequests)) {
			Show show = episodeRequest.getShow();
			Map<Integer, Pair<TreeSet<Episode>, Boolean>> map = eps.get(show.getName());
			if (map == null) {
				eps.put(show.getName(), createShowEpisodesMap(show));
			}

		}
		return eps;
	}

	private Map<Integer, Pair<TreeSet<Episode>, Boolean>> createShowEpisodesMap(Show show) {
		Map<Integer, Pair<TreeSet<Episode>, Boolean>> map = new HashMap<>();
		Show persistedShow = showDao.find(show.getId());
		if (persistedShow == null) {
			throw new MediaRSSException("Show " + show + " is not found in the database");
		}
		for (Episode episode : persistedShow.getEpisodes()) {
			int season = episode.getSeason();
			Pair<TreeSet<Episode>, Boolean> pair = map.get(season);
			if (pair == null) {
				TreeSet<Episode> set = new TreeSet<>(new EpisodesComparator());
				set.add(episode);
				pair = new MutablePair<>(set, episode.isUnAired());
				map.put(season, pair);
			} else {
				pair.setValue(pair.getValue() || episode.isUnAired());
				pair.getKey().add(episode);
			}
		}
		return map;
	}

	private void expandFullShowRequests(Set<ShowRequest> episodeRequests, Map<String, Map<Integer, Pair<TreeSet<Episode>, Boolean>>> eps) {
		// if no season given, should replace with the available seasons
		for (ShowRequest episodeRequest : new HashSet<>(episodeRequests)) {
			if (episodeRequest instanceof FullShowRequest) {
				Show show = episodeRequest.getShow();
				Map<Integer, Pair<TreeSet<Episode>, Boolean>> map = eps.get(show.getName());
				episodeRequests.remove(episodeRequest);
				for (Map.Entry<Integer, Pair<TreeSet<Episode>, Boolean>> entry : map.entrySet()) {
					episodeRequests.add(new FullSeasonRequest(show.getName(), show, episodeRequest.getQuality(), entry.getKey()));
				}
			}
		}
	}

	private void expandFullSeasonRequests(Set<ShowRequest> episodeRequests, Map<String, Map<Integer, Pair<TreeSet<Episode>, Boolean>>> eps) {
		// if requesting a full season, also try to download the episodes 1 by 1, not always there is 1 torrent of the full season
		// special case - if there are un-aired episodes in a season then there is no need to try download the full season as a single torrent
		for (ShowRequest showRequest : new HashSet<>(episodeRequests)) {
			if (showRequest instanceof FullSeasonRequest) {
				Show show = showRequest.getShow();
				Map<Integer, Pair<TreeSet<Episode>, Boolean>> map = eps.get(show.getName());
				int season = ((FullSeasonRequest) showRequest).getSeason();
				Pair<TreeSet<Episode>, Boolean> pair = map.get(season);
				if (pair != null) {
					// if there is un-aired episodes in that season, remove that request
					if (pair.getValue()) {
						episodeRequests.remove(showRequest);
					}

					for (Episode episode : pair.getKey()) {
						// skip full season episodes
						if (episode.getEpisode() > -1) {
							episodeRequests.add(new SingleEpisodeRequest(show.getName(), show, showRequest.getQuality(), season, episode.getEpisode()));
						}
					}
				}
			}
		}
	}
}
