package rss.dao;

import rss.entities.*;

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

	UserMovie findUserMovie(long movieId, User user);

	Movie findByImdbUrl(String imdbUrl);

	List<UserMovie> findFutureUserMovies(User user);

	Collection<User> findUsersForFutureMovie(Movie movie);

	Collection<UserMovie> findUserMovies(long movieId);
}
