package rss.services.downloader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import rss.dao.MovieDao;
import rss.dao.TorrentDao;
import rss.dao.UserTorrentDao;
import rss.entities.*;
import rss.services.searchers.SearchResult;
import rss.services.movies.IMDBParseResult;
import rss.services.movies.IMDBService;
import rss.services.requests.MovieRequest;
import rss.services.searchers.TorrentSearcher;
import rss.util.CollectionUtils;

import java.util.*;

/**
 * User: Michael Dikman
 * Date: 03/12/12
 * Time: 09:10
 */
@Service("moviesTorrentEntriesDownloader")
public class MoviesTorrentEntriesDownloader extends TorrentEntriesDownloader<Movie, MovieRequest> {

	@Autowired
	private MovieDao movieDao;

	@Autowired
	@Qualifier("compositeMoviesSearcher")
	private TorrentSearcher<MovieRequest, Movie> compositeMoviesSearcher;

	@Autowired
	private TorrentDao torrentDao;

	@Autowired
	private UserTorrentDao userTorrentDao;

	@Autowired
	private IMDBService imdbService;

	@Override
	protected SearchResult<Movie> downloadTorrent(MovieRequest movieRequest) {
		return compositeMoviesSearcher.search(movieRequest);
	}

	@Override
	protected boolean validateSearchResult(MovieRequest movieRequest, SearchResult<Movie> searchResult) {
		// if there is no IMDB ID - skip this movie
		if (searchResult.getMetaData().getImdbUrl() == null) {
			logService.info(this.getClass(), String.format("Skipping movie '%s' because no IMDB url found", searchResult.getTorrent().getTitle()));
			return false;
		}

		return true;
	}

	@Override
	protected List<Movie> processSearchResults(Collection<Pair<MovieRequest, SearchResult<Movie>>> results) {
		// first group by search result imdbid, because there might be duplications
		Map<String, List<Pair<MovieRequest, SearchResult<Movie>>>> imdbIdMap = new HashMap<>();
		for (Pair<MovieRequest, SearchResult<Movie>> pair : results) {
			SearchResult<Movie> searchResult = pair.getValue();
			CollectionUtils.safeListPut(imdbIdMap, searchResult.getMetaData().getImdbUrl(), pair);
		}

		List<Movie> res = new ArrayList<>();
		for (Map.Entry<String, List<Pair<MovieRequest, SearchResult<Movie>>>> entry : imdbIdMap.entrySet()) {
			String imdbId = entry.getKey();
			List<Pair<MovieRequest, SearchResult<Movie>>> pairs = entry.getValue();

			// if its from the ui, the movie is already downloaded and stored. otherwise if its not already stored need to download it
			Movie persistedMovie = getMovieHelper(imdbId);
			if (persistedMovie == null) {
				continue;
			}

			for (Pair<MovieRequest, SearchResult<Movie>> pair : pairs) {
				SearchResult<Movie> searchResult = pair.getValue();
				MovieRequest movieRequest = pair.getKey();

				// if there is IMDB ID in the original request (meaning it came from ui)
				// if there is no IMDB ID in the original request (meaning it came from the movies job)
				// download imdb page (for the first time) - using the getMovieHelper()
				if (movieRequest.getImdbId() != null) {
					// compare IMDB ID and skip if no match
					if (!movieRequest.getImdbId().equals(searchResult.getMetaData().getImdbUrl())) {
						logService.info(this.getClass(), String.format("Skipping movie '%s' because IMDB ID '%s' doesn't match the requested one '%s'",
								searchResult.getTorrent().getTitle(), searchResult.getMetaData().getImdbUrl(), movieRequest.getImdbId()));
						continue;
					}
				}

				Torrent persistedTorrent = persistTorrent(searchResult.getTorrent(), movieRequest.getHash());

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

	private Torrent persistTorrent(Torrent torrent, String hash) {
		Torrent persistedTorrent = torrentDao.findByUrl(torrent.getUrl());
		if (persistedTorrent == null) {
			torrentDao.persist(torrent);
			persistedTorrent = torrent;
		}

		// set the quality of the torrent
		for (MediaQuality mediaQuality : MediaQuality.topToBottom()) {
			if (persistedTorrent.getTitle().contains(mediaQuality.toString())) {
				persistedTorrent.setQuality(mediaQuality);
				break;
			}
		}
		persistedTorrent.setHash(hash);
		return persistedTorrent;
	}

	@Override
	protected void processMissingRequests(Collection<MovieRequest> missing) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	protected Collection<Movie> preDownloadPhase(Set<MovieRequest> requests, boolean forceDownload) {
		Set<Movie> result = new HashSet<>();

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
			result.add(movie);
			requests.remove(byHash.get(torrent.getHash()));
		}

		return result;
	}
}
