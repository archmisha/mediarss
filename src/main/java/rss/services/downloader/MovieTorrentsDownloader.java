package rss.services.downloader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.dao.*;
import rss.entities.*;
import rss.services.movies.IMDBParseResult;
import rss.services.movies.IMDBService;
import rss.services.requests.movies.MovieRequest;
import rss.services.searchers.MovieSearcher;
import rss.services.searchers.SearchResult;
import rss.services.subtitles.SubtitleLanguage;
import rss.util.CollectionUtils;

import java.util.*;

/**
 * User: Michael Dikman
 * Date: 03/12/12
 * Time: 09:10
 */
@Service
public class MovieTorrentsDownloader extends BaseDownloader<MovieRequest, Movie> {

	@Autowired
	private MovieDao movieDao;

	@Autowired
	private MovieSearcher movieSearcher;

	@Autowired
	private TorrentDao torrentDao;

	@Autowired
	private UserTorrentDao userTorrentDao;

	@Autowired
	private IMDBService imdbService;

	@Override
	protected SearchResult downloadTorrent(MovieRequest movieRequest) {
		return movieSearcher.search(movieRequest);
	}

	@Override
	protected boolean validateSearchResult(MovieRequest movieRequest, SearchResult searchResult) {
		// if there is no IMDB ID - skip this movie
		if (getImdbId(searchResult) == null) {
			logService.info(this.getClass(), String.format("Skipping movie '%s' because no IMDB url found", movieRequest.getTitle()));
			return false;
		}

		return true;
	}

	@Override
	protected List<Movie> processSearchResults(Collection<Pair<MovieRequest, SearchResult>> results) {
		// first group by search result imdbid, because there might be duplications
		Map<String, List<Pair<MovieRequest, SearchResult>>> imdbIdMap = new HashMap<>();
		for (Pair<MovieRequest, SearchResult> pair : results) {
			SearchResult searchResult = pair.getValue();
			CollectionUtils.safeListPut(imdbIdMap, getImdbId(searchResult), pair);
		}

		List<Movie> res = new ArrayList<>();
		for (Map.Entry<String, List<Pair<MovieRequest, SearchResult>>> entry : imdbIdMap.entrySet()) {
			String imdbId = entry.getKey();
			List<Pair<MovieRequest, SearchResult>> pairs = entry.getValue();

			// if its from the ui, the movie is already downloaded and stored. otherwise if its not already stored need to download it
			Movie persistedMovie = getMovieHelper(imdbId);
			if (persistedMovie == null) {
				continue;
			}

			for (Pair<MovieRequest, SearchResult> pair : pairs) {
				SearchResult searchResult = pair.getValue();
				MovieRequest movieRequest = pair.getKey();

				// if there is IMDB ID in the original request (meaning it came from ui)
				// if there is no IMDB ID in the original request (meaning it came from the movies job)
				// download imdb page (for the first time) - using the getMovieHelper()
				if (movieRequest.getImdbId() != null) {
					// compare IMDB ID and skip if no match
					if (!movieRequest.getImdbId().equals(imdbId)) {
						logService.info(this.getClass(), String.format("Skipping movie '%s' because IMDB ID '%s' doesn't match the requested one '%s'",
								movieRequest.getTitle(), imdbId, movieRequest.getImdbId()));
						continue;
					}
				}

				for (Torrent torrent : searchResult.<Torrent>getDownloadables()) {
					Torrent persistedTorrent = torrentDao.findByUrl(torrent.getUrl());
					if (persistedTorrent == null) {
						torrentDao.persist(torrent);
						persistedTorrent = torrent;
					}

					if (persistedMovie.getTorrentIds().isEmpty()) {
						// if movie already existed and had no torrents - this is the case where it is a future movie request by user
						Collection<User> users = movieDao.findUsersForFutureMovie(persistedMovie);
						if (!users.isEmpty()) {
							logService.info(getClass(), "Detected a FUTURE movie " + persistedMovie.getName() + " for users: " + StringUtils.join(users, ", "));
							for (User user : users) {
								UserMovieTorrent userTorrent = new UserMovieTorrent();
								userTorrent.setUser(user);
								userTorrent.setAdded(new Date());
								userTorrent.setTorrent(persistedTorrent);
								userTorrentDao.persist(userTorrent);
							}
						}
					}

					persistedMovie.getTorrentIds().add(persistedTorrent.getId());
				}
			}

			res.add(persistedMovie);
		}
		return res;
	}

	private Movie getMovieHelper(String imdbId) {
		Movie movie = movieDao.findByImdbUrl(imdbId);
		if (movie == null) {
			// if there is no IMDB ID in the original request (meaning it came from the movies job)
			// download imdb page (for the first time)
			IMDBParseResult imdbParseResult = imdbService.downloadMovieFromIMDB(imdbId);

			// ignore future movies which are not release yet
			if (imdbParseResult.isComingSoon()) {
				logService.info(getClass(), "Skipping movie '" + imdbParseResult.getName() + "' because it is not yet released (imdbId=" + imdbId + ")");
				return null;
			}

			// check for number of reviewers for the movie
			if (imdbParseResult.getViewers() != -1 && imdbParseResult.getViewers() < 1000) {
				logService.info(getClass(), "Skipping movie '" + imdbParseResult.getName() + "' due to low number of viewers in imdb: " + imdbParseResult.getViewers());
				return null;
			}

			movie = new Movie(imdbParseResult.getName(), imdbId, imdbParseResult.getYear());
			movieDao.persist(movie);
		}
		return movie;
	}

	@Override
	protected void processMissingRequests(Collection<MovieRequest> missing) {

	}

	@Override
	protected Collection<Movie> preDownloadPhase(Set<MovieRequest> requests, boolean forceDownload) {
		Set<Movie> cachedMovies = skipCachedMovies(requests);
//		handleSubtitles(cachedMovies);
		return cachedMovies;
	}

	private Set<Movie> skipCachedMovies(Set<MovieRequest> requests) {
		Set<Movie> cachedMovies = new HashSet<>();

		Map<String, MovieRequest> byHash = new HashMap<>();
		for (MovieRequest request : requests) {
			byHash.put(request.getHash(), request);
		}

		// the name of the torrent file is not necessary the same as in the request which comes from search in torrentz.com
		// sometimes . - or spaces can be misplaces
		Collection<Torrent> torrents = torrentDao.findByHash(byHash.keySet());
		for (Torrent torrent : torrents) {
			Movie movie = movieDao.find(torrent);
			logService.info(this.getClass(), String.format("Movie \"%s\" was found in cache (torrent: \"%s\")", movie, torrent.getTitle()));
			cachedMovies.add(movie);
			requests.remove(byHash.get(torrent.getHash()));
		}
		return cachedMovies;
	}

	private String getImdbId(SearchResult searchResult) {
		for (Torrent torrent : searchResult.<Torrent>getDownloadables()) {
			if (!StringUtils.isBlank(torrent.getImdbId())) {
				return torrent.getImdbId();
			}
		}
		return null;
	}

//	private void handleSubtitles(Map<Movie, List<SubtitleLanguage>> languagesPerMovie, Movie movie, Torrent torrent) {
//		List<SubtitleLanguage> languages = getSubtitleLanguagesForShow(languagesPerShow, showRequest.getShow());
//		if (languages.isEmpty()) {
//			return;
//		}
//		List<SubtitlesRequest> subtitlesRequests = new ArrayList<>();
//
//
//		if (!subtitlesRequests.isEmpty()) {
//			subtitlesService.downloadSubtitlesAsync(subtitlesRequests);
//		}
//	}
//
//	private void handleSubtitles(Set<Movie> movies) {
//		List<SubtitlesRequest> subtitlesRequests = new ArrayList<>();
//
//		List<SubtitleLanguage> languages = getSubtitleLanguagesForShow(languagesPerShow, show);
//		if (!languages.isEmpty()) {
//			subtitlesRequests.add(new SubtitlesSingleEpisodeRequest(torrent, show, episode.getSeason(), episode.getEpisode(), languages, episode.getAirDate()));
//		}
//
//		if (!subtitlesRequests.isEmpty()) {
//			subtitlesService.downloadSubtitlesAsync(subtitlesRequests);
//		}
//	}
//
//	private List<SubtitleLanguage> getSubtitleLanguagesForShow(Map<Show, List<SubtitleLanguage>> languagesPerShow, Movie movie) {
//		if (!languagesPerShow.containsKey(show)) {
//			if (sessionService.isUserLogged()) {
//				SubtitleLanguage subtitleLanguage = userDao.find(sessionService.getLoggedInUserId()).getSubtitles();
//				if (subtitleLanguage != null) {
//					languagesPerShow.put(show, Collections.singletonList(subtitleLanguage));
//				} else {
//					languagesPerShow.put(show, Collections.<SubtitleLanguage>emptyList());
//				}
//			} else {
//				languagesPerShow.put(show, subtitlesDao.getSubtitlesLanguages(movie));
//			}
//		}
//		return languagesPerShow.get(show);
//	}
}
