package rss.dao;

import rss.entities.*;

import java.util.Collection;
import java.util.Date;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface MovieDao extends Dao<Movie> {

    Collection<Movie> findUploadedSince(Date uploadedDate);

    Movie findByName(String name);

    Movie find(Torrent torrent);

	void persist(UserMovie userMovie);

	UserMovie findUserMovie(long movieId, User user);
}
