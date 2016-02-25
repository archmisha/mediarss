package rss.movies;

import org.apache.commons.lang3.tuple.Pair;
import rss.movies.imdb.IMDBAutoCompleteItem;
import rss.movies.imdb.IMDBParseResult;
import rss.torrents.Movie;
import rss.torrents.Torrent;
import rss.user.User;

import java.util.Collection;
import java.util.List;

/**
 * User: dikmanm
 * Date: 08/03/13 15:38
 */
public interface MovieService {

	List<UserMovieVO> getSearchCompletedMovies(long[] ids);

	List<UserMovieVO> getUserMovies(User user);

	List<UserMovieVO> getAvailableMovies(User loggedInUser);

	Pair<Movie, Boolean> addFutureMovieDownload(User user, String imdbId);

	void downloadMovie(Movie movie);

	void addMovieDownload(User user, long movieId, long torrentId);

	void downloadLatestMovies();

	Collection<IMDBAutoCompleteItem> search(User user, String query);

	void downloadUserMovies();

	void addMovie(Movie movie, IMDBParseResult imdbParseResult);

	String getImdbPreviewPage(Movie movie);

	Movie findByImdbUrl(String imdbId);

	MovieServiceFactory factory();

	Movie find(Torrent torrent);

	void delete(Movie movie);

	Movie find(Long movieId);

	Collection<UserMovie> findUserMoviesByMovieId(Long movieId);
}
