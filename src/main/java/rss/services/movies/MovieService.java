package rss.services.movies;

import rss.controllers.vo.UserMovieVO;
import rss.entities.Movie;
import rss.entities.User;

import java.util.ArrayList;

/**
 * User: dikmanm
 * Date: 08/03/13 15:38
 */
public interface MovieService {

	ArrayList<UserMovieVO> getFutureUserMovies(User user);

	ArrayList<UserMovieVO> getUserMovies(User loggedInUser);

	Movie addFutureMovieDownload(User user, String imdbId);

	void markMovieViewed(User user, long movieId);

	String getImdbPreviewPage(Movie movie);
}
