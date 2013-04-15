package rss.services.downloader;

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
import rss.SubtitleLanguage;
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
import rss.services.shows.ShowService;

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
	@Transactional(propagation = Propagation.REQUIRED)
	protected List<Episode> onTorrentFound(ShowRequest episodeRequest, SearchResult<Episode> searchResult) {
		Show persistedShow = showDao.find(episodeRequest.getShow().getId());
		Torrent torrent = searchResult.getTorrent();

		List<Episode> persistedEpisodes;
		if (episodeRequest instanceof SingleEpisodeRequest) {
			persistedEpisodes = Collections.singletonList(persistEpisodeRequest(persistedShow, (SingleEpisodeRequest) episodeRequest));
		} else if (episodeRequest instanceof FullSeasonRequest) {
			persistedEpisodes = Collections.singletonList(persistEpisodeRequest(persistedShow, (FullSeasonRequest) episodeRequest));
		} else {
			DoubleEpisodeRequest doubleEpisodeRequest = (DoubleEpisodeRequest) episodeRequest;
			persistedEpisodes = Arrays.asList(
					persistEpisodeRequest(persistedShow, doubleEpisodeRequest.getEpisode1()),
					persistEpisodeRequest(persistedShow, doubleEpisodeRequest.getEpisode2()));
		}

		// sometimes the same torrent returned from search for different episodes
		// it can happen when there are torrents like s01e01-e04 will be returned for s01e(-1) request also
		Torrent persistedTorrent = torrentDao.findByUrl(torrent.getUrl());
		if (persistedTorrent == null) {
			torrentDao.persist(torrent);
			persistedTorrent = torrent;

			// handle subtitles - cant try-catch here, cuz it tried to insert the new entities first and if failed we
			// don't know and un-persisted episode with torrent null is returned! if want try catch need in separate transaction or something
			// download subtitles only if enabled for this show for any user
			for (Episode persistedEpisode : persistedEpisodes) {
				List<SubtitleLanguage> subtitlesLanguages = episodeDao.getSubtitlesLanguages(persistedEpisode);
				if (!subtitlesLanguages.isEmpty()) {
					subtitlesService.downloadEpisodeSubtitles(torrent, persistedEpisode, subtitlesLanguages);
				}
			}
		}

		for (Episode persistedEpisode : persistedEpisodes) {
			persistedEpisode.getTorrentIds().add(persistedTorrent.getId());
		}
		return persistedEpisodes;
	}

	private Episode persistEpisodeRequest(Show show, EpisodeRequest episodeRequest) {
		Episode persistedEpisode = episodeDao.find(episodeRequest);
		if (persistedEpisode == null) {
			if (episodeRequest instanceof SingleEpisodeRequest) {
				persistedEpisode = new Episode(episodeRequest.getSeason(), ((SingleEpisodeRequest) episodeRequest).getEpisode());
			} else {
				persistedEpisode = new Episode(episodeRequest.getSeason(), -1);
			}
			persistedEpisode.setShow(show);
			episodeDao.persist(persistedEpisode);
			show.getEpisodes().add(persistedEpisode);
		}
		return persistedEpisode;
	}

	@Override
	// persisting new shows here - must be persisted before the new transaction opens for each download
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	protected Collection<Episode> preDownloadPhase(Set<ShowRequest> episodeRequests) {
		// expand full season requests into the specific episode requests
		// iterating on a copy
		Map<String, Map<Integer, Pair<Set<Episode>, Boolean>>> eps = new HashMap<>();
		for (ShowRequest episodeRequest : new HashSet<>(episodeRequests)) {
			Show show = episodeRequest.getShow();
			if (show == null) {
				throw new ShowNotFoundException("Show not found: " + episodeRequest.getTitle());
			}

			// substitute show name with alias and seasons if needed
			showService.transformEpisodeRequest(episodeRequest);

			Map<Integer, Pair<Set<Episode>, Boolean>> map = eps.get(show.getName());
			if (map == null) {
				map = new HashMap<>();
				eps.put(show.getName(), map);

				Show persistedShow = showDao.find(show.getId());
				if (persistedShow == null) {
					throw new MediaRSSException("Show " + show + " is not found in the database");
				}
				for (Episode episode : persistedShow.getEpisodes()) {
					int season = episode.getSeason();
					Pair<Set<Episode>, Boolean> pair = map.get(season);
					if (pair == null) {
						Set<Episode> set = new HashSet<>();
						set.add(episode);
						pair = new MutablePair<>(set, episode.isUnAired());
						map.put(season, pair);
					} else {
						pair.setValue(pair.getValue() || episode.isUnAired());
						pair.getKey().add(episode);
					}
				}
			}

			// if no season given, should replace with the available seasons
			if (episodeRequest instanceof FullShowRequest) {
				episodeRequests.remove(episodeRequest);
				for (Map.Entry<Integer, Pair<Set<Episode>, Boolean>> entry : map.entrySet()) {
					episodeRequests.add(new FullSeasonRequest(show.getName(), show, episodeRequest.getQuality(), entry.getKey()));
				}
			}
		}

		// if requesting a full season, also try to download the episodes 1 by 1, not always there is 1 torrent of the full season
		// special case - if there are un-aired episodes in a season then there is no need to try download the full season as a single torrent
		for (ShowRequest showRequest : new HashSet<>(episodeRequests)) {
			if (showRequest instanceof FullSeasonRequest) {
				Show show = showRequest.getShow();
				Map<Integer, Pair<Set<Episode>, Boolean>> map = eps.get(show.getName());
				int season = ((EpisodeRequest) showRequest).getSeason();
				Pair<Set<Episode>, Boolean> pair = map.get(season);
				if (pair != null) {
					// if there is un-aired episodes in that season, remove that request
					if (pair.getValue()) {
						episodeRequests.remove(showRequest);
					}

					for (Episode episode : pair.getKey()) {
						// skip fill season episodes
						if (episode.getEpisode() > -1) {
							episodeRequests.add(new SingleEpisodeRequest(show.getName(), show, showRequest.getQuality(), season, episode.getEpisode()));
						}
					}
				}
			}
		}

		Map<Episode, EpisodeRequest> episodesMap = new HashMap<>();
		for (ShowRequest episodeRequest : episodeRequests) {
			Episode episode = episodeDao.find((EpisodeRequest) episodeRequest);
			if (episode != null) {
				episodesMap.put(episode, (EpisodeRequest) episodeRequest);
			}
		}

		// skip un-aired episodes
		for (Map.Entry<Episode, EpisodeRequest> entry : new ArrayList<>(episodesMap.entrySet())) {
			Episode episode = entry.getKey();
			if (episode.isUnAired()) {
				EpisodeRequest episodeRequest = entry.getValue();
				episodeRequests.remove(episodeRequest);
				// removing to skip in the following iteration loop
				episodesMap.remove(episode);
				logService.info(getClass(), "Skipping downloading '" + episodeRequest.toString() + "' - still un-aired");
			}
		}

		// add double episode requests
		MediaQuality quality = episodeRequests.iterator().next().getQuality();
		for (Map.Entry<String, Map<Integer, Pair<Set<Episode>, Boolean>>> showEntry : eps.entrySet()) {
			String showName = showEntry.getKey();
			for (Map.Entry<Integer, Pair<Set<Episode>, Boolean>> entry : showEntry.getValue().entrySet()) {
				Integer season = entry.getKey();
				ArrayList<Episode> episodes = new ArrayList<>(entry.getValue().getKey());
				if (episodes.size() > 1) {
					Collections.sort(episodes, new EpisodesComparator());
					Episode ep1 = episodes.get(0);
					for (int i = 1; i < episodes.size(); ++i) {
						Episode ep2 = episodes.get(i);
						if (ep1.getAirDate() != null && ep2.getAirDate() != null && ep1.getAirDate().equals(ep2.getAirDate())) {
							DoubleEpisodeRequest doubleEpisodeRequest = new DoubleEpisodeRequest(showName, ep1.getShow(), quality, season,
									(SingleEpisodeRequest) episodesMap.get(ep1), (SingleEpisodeRequest) episodesMap.get(ep2));
							logService.info(getClass(), "Adding a double episode request: " + doubleEpisodeRequest.toString());
							episodeRequests.add(doubleEpisodeRequest);
						}

						ep1 = ep2;
					}
				}
			}
		}

		// prepare cached episodes
		Set<Episode> cachedEpisodes = new HashSet<>();
		for (Map.Entry<Episode, EpisodeRequest> entry : new ArrayList<>(episodesMap.entrySet())) {
			Episode episode = entry.getKey();
			if (!episode.getTorrentIds().isEmpty()) {
				logService.debug(this.getClass(), "Episode \"" + episode + "\" was found in cache");
				cachedEpisodes.add(episode);

//				Set<MediaQuality> qualities = new HashSet<>();
//				for (Torrent torrent : torrentDao.findByIds(episode.getTorrentIds())) {
//					qualities.add(torrent.getQuality());
//				}

				// for episodes older than 2 weeks - no way a better quality will be published anyway, so remove from requests
				// whatever we have is good enough
//				qualities.addAll(Arrays.asList(MediaQuality.values()));

//				for (MediaQuality quality : qualities) {
//					episodeRequests.remove(new EpisodeRequest(episode.getName(), episode.getShow(), quality, episode.getSeason(), episode.getEpisode()));
//				}
				episodeRequests.remove(entry.getValue());

				// removing to skip in the following iteration loop
				episodesMap.remove(episode);
			}
		}
		return cachedEpisodes;
	}
}
