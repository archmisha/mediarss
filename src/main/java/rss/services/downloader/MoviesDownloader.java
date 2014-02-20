package rss.services.downloader;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import rss.dao.MovieDao;
import rss.dao.TorrentDao;
import rss.entities.Movie;
import rss.entities.Torrent;
import rss.services.movies.IMDBParseResult;
import rss.services.movies.IMDBService;
import rss.services.movies.MovieService;
import rss.services.requests.movies.MovieRequest;
import rss.services.searchers.SearchResult;
import rss.util.CollectionUtils;

import java.util.*;

/**
 * User: dikmanm
 * Date: 18/05/13 08:46
 */
public abstract class MoviesDownloader extends BaseDownloader<MovieRequest, Movie> {

	@Autowired
	private MovieDao movieDao;

	@Autowired
	private MovieService movieService;

	@Autowired
	private TorrentDao torrentDao;

	@Autowired
	private IMDBService imdbService;

	@Override
	protected boolean isSingleTransaction() {
		return true;
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
	protected Collection<Movie> processSingleSearchResult(MovieRequest mediaRequest, SearchResult searchResult) {
		throw new UnsupportedOperationException();
	}

	// movies are processes at the end in bulk, to group by IMDB ID, thats why for a single result we return empty list and not process it yet
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
					Torrent persistedTorrent = torrentDao.findByHash(torrent.getHash());
					if (persistedTorrent == null) {
						torrentDao.persist(torrent);
						persistedTorrent = torrent;
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
			if (!imdbParseResult.isFound()) {
				return null;
			}

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

			movie = new Movie(imdbParseResult.getName(), imdbId, imdbParseResult.getYear(), imdbParseResult.getReleaseDate());
			movieService.addMovie(movie, imdbParseResult);
		}
		return movie;
	}

	@Override
	protected void processSingleMissingRequest(MovieRequest missing) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void processMissingRequests(Collection<MovieRequest> missing) {
	}

	@Override
	protected Collection<Movie> preDownloadPhase(Set<MovieRequest> requests, boolean forceDownload) {
		Set<Movie> cachedMovies = skipCachedMovies(requests);
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
			if (torrent.getImdbId() != null) {
				return torrent.getImdbId();
			}
		}
		return null;
	}
}
