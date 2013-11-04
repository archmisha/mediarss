package rss.services.downloader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.EpisodesComparator;
import rss.MediaRSSException;
import rss.ShowNotFoundException;
import rss.dao.*;
import rss.entities.Episode;
import rss.entities.MediaQuality;
import rss.entities.Show;
import rss.entities.Torrent;
import rss.services.SessionService;
import rss.services.requests.episodes.*;
import rss.services.requests.subtitles.SubtitlesDoubleEpisodeRequest;
import rss.services.requests.subtitles.SubtitlesRequest;
import rss.services.requests.subtitles.SubtitlesSingleEpisodeRequest;
import rss.services.searchers.SearchResult;
import rss.services.searchers.composite.EpisodeSearcher;
import rss.services.shows.EpisodesMapper;
import rss.services.shows.ShowService;
import rss.services.subtitles.SubtitleLanguage;
import rss.services.subtitles.SubtitlesService;
import rss.util.CollectionUtils;
import rss.util.DateUtils;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * User: Michael Dikman
 * Date: 02/12/12
 * Time: 23:49
 */
@Service
public class EpisodeTorrentsDownloader extends BaseDownloader<ShowRequest, Episode> {

	@Autowired
	private EpisodeDao episodeDao;

	@Autowired
	private TorrentDao torrentDao;

	@Autowired
	private EpisodeSearcher episodeSearcher;

	@Autowired
	private ShowService showService;

	@Autowired
	private ShowDao showDao;

	@Autowired
	private SubtitlesService subtitlesService;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private SubtitlesDao subtitlesDao;

	@Autowired
	private UserDao userDao;

	@Override
	protected boolean isSingleTransaction() {
		return false;
	}

	@Override
	protected SearchResult downloadTorrent(ShowRequest episodeRequest) {
		return episodeSearcher.search(episodeRequest);
	}

	@Override
	protected void processSingleMissingRequest(ShowRequest missing) {
		for (Episode episode : episodeDao.find(missing)) {
			episode.setScanDate(new Date());
		}
	}

	@Override
	protected void processMissingRequests(Collection<ShowRequest> missing) {
		throw new UnsupportedOperationException();
//		for (Episode episode : episodeDao.findByRequests(missing)) {
//			episode.setScanDate(new Date());
//		}
	}

	@Override
	protected boolean validateSearchResult(ShowRequest mediaRequest, SearchResult searchResult) {
		return true;
	}

	@Override
	protected Collection<Episode> processSingleSearchResult(ShowRequest showRequest, SearchResult searchResult) {
		// todo: assuming there is only one here. fix that
		Torrent torrent = searchResult.<Torrent>getDownloadables().get(0);

		List<Episode> persistedEpisodes = episodeDao.find(showRequest);

		// sometimes the same torrent returned from search for different episodes
		// it can happen when there are torrents like s01e01-e04 will be returned for s01e(-1) request also
		Torrent persistedTorrent = torrentDao.findByHash(torrent.getHash());
		if (persistedTorrent == null) {
			torrentDao.persist(torrent);
			persistedTorrent = torrent;

			handleSubtitles(showRequest, torrent, persistedEpisodes);
		}

		for (Episode persistedEpisode : persistedEpisodes) {
			persistedEpisode.getTorrentIds().add(persistedTorrent.getId());
			persistedEpisode.setScanDate(new Date());
		}

		return persistedEpisodes;
	}

	@Override
	protected List<Episode> processSearchResults(Collection<Pair<ShowRequest, SearchResult>> results) {
		throw new UnsupportedOperationException();
//		Map<Show, List<SubtitleLanguage>> languagesPerShow = new HashMap<>();
//		List<Episode> res = new ArrayList<>();
//		for (Pair<ShowRequest, SearchResult> pair : results) {
//			SearchResult searchResult = pair.getValue();
//			ShowRequest showRequest = pair.getKey();
//
//			res.addAll(persistedEpisodes);
//		}
//		return res;
	}

	@Override
	// persisting new shows here - must be persisted before the new transaction opens for each download
	protected Collection<Episode> preDownloadPhase(Set<ShowRequest> episodeRequests, boolean forceDownload) {
		Long userId = null;

		// substitute show name with alias and seasons if needed
		for (ShowRequest episodeRequest : new HashSet<>(episodeRequests)) {
			showService.transformEpisodeRequest(episodeRequest);
			if (userId == null) {
				userId = episodeRequest.getUserId();
			}
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

		// for cached episodes, check if need to search for any subtitles
		handleSubtitles(cachedEpisodes, userId);

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
					Long userId = episodesMap.get(episode).iterator().next().getUserId();
					EpisodeRequest episodeRequest = new FullSeasonRequest(userId, show.getName(), show, MediaQuality.HD720P, episode.getSeason());
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
				createEpisodesToRequestsMapHelper(episode, episodeRequest, episodeRequests, episodesMap, forceDownload);
			} else if (episodeRequest instanceof DoubleEpisodeRequest) {
				EpisodesMapper episodesMapper = eps2.get(episodeRequest.getShow().getName());
				Episode episode1 = episodesMapper.get(episodeRequest.getSeason(), ((DoubleEpisodeRequest) episodeRequest).getEpisode1());
				Episode episode2 = episodesMapper.get(episodeRequest.getSeason(), ((DoubleEpisodeRequest) episodeRequest).getEpisode2());
				createEpisodesToRequestsMapHelper(episode1, episodeRequest, episodeRequests, episodesMap, forceDownload);
				createEpisodesToRequestsMapHelper(episode2, episodeRequest, episodeRequests, episodesMap, forceDownload);
			} else if (episodeRequest instanceof FullSeasonRequest) {
				Map<Integer, Pair<TreeSet<Episode>, Boolean>> map = eps.get(episodeRequest.getShow().getName());
				if (!map.containsKey(episodeRequest.getSeason())) {
					logService.info(getClass(), "Skipping request '" + episodeRequest + "' because no such episodes exist");
					episodeRequests.remove(episodeRequest);
				} else {
					for (Episode episode : map.get(episodeRequest.getSeason()).getKey()) {
						createEpisodesToRequestsMapHelper(episode, episodeRequest, episodeRequests, episodesMap, forceDownload);
					}
				}
			} else {
				throw new InvalidParameterException("Unhandled class of Episode request: " + episodeRequest.getClass());
			}
		}
		return episodesMap;
	}

	private void createEpisodesToRequestsMapHelper(Episode episode,
												   EpisodeRequest episodeRequest,
												   Set<ShowRequest> episodeRequests,
												   Map<Episode, Set<EpisodeRequest>> episodesMap,
												   boolean forceDownload) {
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
	}

	private void deleteTorrents(Episode episode) {
		showService.disconnectTorrentsFromEpisode(episode);

		// also setting scanDate to null, cuz if we used forceDownload and removed the torrents and then the search died in the middle
		// next time scanDate will think we still have the previous torrents and will skip downloading
		episode.setScanDate(null);
	}

	private void skipScannedEpisodes(Set<ShowRequest> episodeRequests,
									 Map<String, Map<Integer, Pair<TreeSet<Episode>, Boolean>>> eps,
									 Map<Episode, Set<EpisodeRequest>> episodesMap) {
		if (episodeRequests.isEmpty()) {
			return;
		}

		Date backlogDate = DateUtils.getPastDate(14);
		for (Map.Entry<Episode, Set<EpisodeRequest>> entry : new ArrayList<>(episodesMap.entrySet())) {
			Episode episode = entry.getKey();
			// skip if already scanned and
			// 1. episode airdate is older than 14 days ago (full season has air date null)
			// 2. if its a full season and its last episode aired 14 days ago (only finished seasons here, unaired seasons filtered before)
			if (episode.getScanDate() != null) {
				boolean shouldRemoveEpisode = false;
				if (episode.getEpisode() > 0 && episode.getAirDate() != null && episode.getAirDate().before(backlogDate)) {
					shouldRemoveEpisode = true;
				} else if (episode.getEpisode() == -1) {
					TreeSet<Episode> episodes = eps.get(episode.getShow().getName()).get(episode.getSeason()).getKey();
					// episode -1 is sorted at the beginning, so only need to check there is more than 1 episode in the list
					if (!episodes.isEmpty()) {
						Episode ep = episodes.last();
						if (ep.getEpisode() > 0) {
							if (episode.getAirDate() != null && episode.getAirDate().before(backlogDate)) {
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
		ShowRequest request = episodeRequests.iterator().next();
		MediaQuality quality = request.getQuality();
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
							DoubleEpisodeRequest doubleEpisodeRequest = new DoubleEpisodeRequest(request.getUserId(), showName, ep1.getShow(), quality, season,
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
					episodeRequests.add(new FullSeasonRequest(episodeRequest.getUserId(), show.getName(), show, episodeRequest.getQuality(), entry.getKey()));
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
							episodeRequests.add(new SingleEpisodeRequest(showRequest.getUserId(), show.getName(), show, showRequest.getQuality(), season, episode.getEpisode()));
						}
					}
				}
			}
		}
	}

	private void handleSubtitles(ShowRequest showRequest, Torrent torrent, List<Episode> persistedEpisodes) {
		Map<Show, List<SubtitleLanguage>> languagesPerShow = new HashMap<>();
		List<SubtitleLanguage> languages = getSubtitleLanguagesForShow(languagesPerShow, showRequest.getShow(), showRequest.getUserId());
		if (languages.isEmpty()) {
			return;
		}

		Set<SubtitlesRequest> subtitlesRequests = new HashSet<>();
		if (showRequest instanceof SingleEpisodeRequest) {
			Episode episode = persistedEpisodes.get(0);
			if (episode.getEpisode() != -1) {
				subtitlesRequests.add(new SubtitlesSingleEpisodeRequest(torrent, showRequest.getShow(), episode.getSeason(), episode.getEpisode(), languages, episode.getAirDate()));
			}
		} else if (showRequest instanceof DoubleEpisodeRequest) {
			Episode episode1 = persistedEpisodes.get(0);
			Episode episode2 = persistedEpisodes.get(1);
			subtitlesRequests.add(new SubtitlesDoubleEpisodeRequest(torrent, showRequest.getShow(), episode1.getSeason(),
					episode1.getEpisode(), episode2.getEpisode(), languages, episode1.getAirDate(), episode2.getAirDate()));
		} else if (showRequest instanceof FullSeasonRequest) {
			// create request for each episode in the season and link to this single torrent
			Map<Integer, Pair<TreeSet<Episode>, Boolean>> showEpisodesMap = createShowEpisodesMap(showRequest.getShow());
			for (Episode episode : showEpisodesMap.get(((FullSeasonRequest) showRequest).getSeason()).getKey()) {
				subtitlesRequests.add(new SubtitlesSingleEpisodeRequest(torrent, showRequest.getShow(), episode.getSeason(), episode.getEpisode(), languages, episode.getAirDate()));
			}
		}

		if (!subtitlesRequests.isEmpty()) {
			subtitlesService.downloadSubtitlesAsync(subtitlesRequests);
		}
	}

	// for full season episode expand to single episodes and create subtitles requests for them
	// the rest group by torrents, if single episode per torrent - create single request
	// if 2 episodes per torrent - its a double request
	private void handleSubtitles(Set<Episode> episodes, Long userId) {
		Map<Show, List<SubtitleLanguage>> languagesPerShow = new HashMap<>();
		Set<SubtitlesRequest> subtitlesRequests = new HashSet<>();
		Map<Torrent, List<Episode>> map = new HashMap<>();
		for (Episode episode : episodes) {
			for (Torrent torrent : torrentDao.find(episode.getTorrentIds())) {
				if (episode.getEpisode() == -1) {
					// create request for each episode in the season and link to this single torrent
					Map<Integer, Pair<TreeSet<Episode>, Boolean>> showEpisodesMap = createShowEpisodesMap(episode.getShow());
					for (Episode curEpisode : showEpisodesMap.get(episode.getSeason()).getKey()) {
						// skip full season episode
						if (curEpisode.getEpisode() == -1) {
							continue;
						}
						Show show = curEpisode.getShow();
						List<SubtitleLanguage> languages = getSubtitleLanguagesForShow(languagesPerShow, show, userId);
						if (!languages.isEmpty()) {
							subtitlesRequests.add(new SubtitlesSingleEpisodeRequest(torrent, show, curEpisode.getSeason(), curEpisode.getEpisode(), languages, curEpisode.getAirDate()));
						}
					}
				} else {
					CollectionUtils.safeListPut(map, torrent, episode);
				}
			}
		}

		for (Map.Entry<Torrent, List<Episode>> entry : map.entrySet()) {
			Torrent torrent = entry.getKey();
			Show show = entry.getValue().get(0).getShow();
			List<SubtitleLanguage> languages = getSubtitleLanguagesForShow(languagesPerShow, show, userId);
			if (!languages.isEmpty()) {
				if (entry.getValue().size() == 1) {
					Episode episode = entry.getValue().get(0);
					subtitlesRequests.add(new SubtitlesSingleEpisodeRequest(torrent, episode.getShow(), episode.getSeason(), episode.getEpisode(), languages, episode.getAirDate()));
				} else if (entry.getValue().size() == 2) {
					Episode episode1 = entry.getValue().get(0);
					Episode episode2 = entry.getValue().get(1);
					subtitlesRequests.add(new SubtitlesDoubleEpisodeRequest(torrent, episode1.getShow(), episode1.getSeason(),
							episode1.getEpisode(), episode2.getEpisode(), languages, episode1.getAirDate(), episode2.getAirDate()));
				} else {
					logService.error(getClass(), "Weird case: " + entry.getValue().size() + " episodes per torrent: " + torrent + ". not downloading subtitles");
				}
			}
		}

		if (!subtitlesRequests.isEmpty()) {
			subtitlesService.downloadSubtitlesAsync(subtitlesRequests);
		}
	}

	private List<SubtitleLanguage> getSubtitleLanguagesForShow(Map<Show, List<SubtitleLanguage>> languagesPerShow, Show show, Long userId) {
		if (!languagesPerShow.containsKey(show)) {
			try {
				if (sessionService.isUserLogged()) {
					getSubtitleLanguagesForShowHelper(sessionService.getLoggedInUserId(), languagesPerShow, show);
				} else {
					languagesPerShow.put(show, subtitlesDao.getSubtitlesLanguages(show));
				}
			} catch (BeansException e) {
				// if user is not logged in and we don't have a session, we will have an exception here
				if (userId != null) {
					getSubtitleLanguagesForShowHelper(userId, languagesPerShow, show);
				} else {
					languagesPerShow.put(show, subtitlesDao.getSubtitlesLanguages(show));
				}
			}
		}
		return languagesPerShow.get(show);
	}

	private void getSubtitleLanguagesForShowHelper(long userId, Map<Show, List<SubtitleLanguage>> languagesPerShow, Show show) {
		SubtitleLanguage subtitleLanguage = userDao.find(userId).getSubtitles();
		if (subtitleLanguage != null) {
			languagesPerShow.put(show, Collections.singletonList(subtitleLanguage));
		} else {
			languagesPerShow.put(show, Collections.<SubtitleLanguage>emptyList());
		}
	}
}
