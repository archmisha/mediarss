package rss.services.movies;

import org.apache.commons.lang3.tuple.Pair;
import rss.controllers.vo.UserMovieVO;
import rss.entities.Movie;
import rss.entities.User;
import rss.services.downloader.DownloadResult;
import rss.services.requests.movies.MovieRequest;

import java.util.Collection;
import java.util.List;

/**
 * User: dikmanm
 * Date: 08/03/13 15:38
 */
public interface MovieService {

	List<UserMovieVO> getSearchCompletedMovies(long[] ids);

//	int getUserMoviesCount(User user);

//	int getAvailableMoviesCount(User user);

	List<UserMovieVO> getUserMovies(User user);

	List<UserMovieVO> getAvailableMovies(User loggedInUser);

	Pair<Movie, Boolean> addFutureMovieDownload(User user, String imdbId);

//	void markMovieViewed(User user, long movieId);

	void addMovieDownload(User user, long movieId, long torrentId);

	DownloadResult<Movie, MovieRequest> downloadLatestMovies();

	Collection<IMDBAutoCompleteItem> search(User user, String query);

	void downloadUserMovies();

	void addMovie(Movie movie, IMDBParseResult imdbParseResult);

	String getImdbPreviewPage(Movie movie);
}
