package rss.services.movies;

import org.apache.commons.lang3.tuple.Pair;
import rss.controllers.vo.UserMovieVO;
import rss.entities.Movie;
import rss.entities.User;
import rss.entities.UserMovie;
import rss.services.downloader.DownloadResult;
import rss.services.requests.MovieRequest;

import java.util.ArrayList;

/**
 * User: dikmanm
 * Date: 08/03/13 15:38
 */
public interface MovieService {

	ArrayList<UserMovieVO> getUserMovies(User user);

	ArrayList<UserMovieVO> getAvailableMovies(User loggedInUser);

	Pair<UserMovie, Boolean> addFutureMovieDownload(User user, String imdbId);

	void markMovieViewed(User user, long movieId);

	Pair<UserMovie, Boolean> addMovieDownload(User user, long movieId);

	DownloadResult<Movie, MovieRequest> downloadLatestMovies();
}
