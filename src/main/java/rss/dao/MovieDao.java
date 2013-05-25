package rss.dao;

import rss.entities.Movie;
import rss.entities.Torrent;
import rss.entities.User;
import rss.entities.UserMovie;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface MovieDao extends Dao<Movie> {

	Collection<Movie> findUploadedSince(Date uploadedDate);

//    Movie findByName(String name);

	Movie find(Torrent torrent);

	void persist(UserMovie userMovie);

	void delete(UserMovie userMovie);

	UserMovie findUserMovie(User user, long movieId);

	Movie findByImdbUrl(String imdbUrl);

	List<UserMovie> findUserMovies(User user, int backlogDays);

	Collection<User> findUsersForFutureMovie(Movie movie);

	Collection<UserMovie> findUserMoviesByMovieId(long movieId);

	Collection<UserMovie> findUserMoviesByIMDBIds(User user, Collection<String> imdbIds);

	int findUserMoviesCount(User user, int backlogDays);
}
