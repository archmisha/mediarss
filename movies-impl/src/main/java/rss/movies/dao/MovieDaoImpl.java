package rss.movies.dao;

import org.springframework.stereotype.Repository;
import rss.ems.dao.BaseDaoJPA;
import rss.movies.UserMovie;
import rss.torrents.Movie;
import rss.torrents.Torrent;
import rss.user.User;
import rss.util.DateUtils;

import java.util.*;

/**
 * User: Michael Dikman
 * Date: 24/05/12
 * Time: 11:05
 */
@Repository
public class MovieDaoImpl extends BaseDaoJPA<Movie> implements MovieDao {

	@Override
	public Collection<Movie> findUploadedSince(Date uploadedDate) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("dateUploaded", uploadedDate);
		return super.findByNamedQueryAndNamedParams("Movie.findByDateUploaded", params);
	}

	@Override
	public Collection<Movie> findOrderedByUploadedSince(int count) {
		Collection<Movie> results = new ArrayList<>();
		for (Object[] arr : super.<Object[]>findByNamedQueryAndNamedParams("Movie.findOrderedByDateUploaded", null, count)) {
			results.add((Movie) arr[0]);
		}
		return results;
	}

	@Override
	public int findUploadedSinceCount(Date uploadedDate) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("dateUploaded", uploadedDate);
		return super.<Movie>findByNamedQueryAndNamedParams("Movie.findByDateUploadedCount", params).size();
	}

	/*@Override
	public Movie findByName(String name) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("name", name);
		return uniqueResult(super.<Movie>findByNamedQueryAndNamedParams("Movie.findByName", params));
	}*/

	@Override
	public Movie find(Torrent torrent) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("torrentId", torrent.getId());
		return uniqueResult(super.<Movie>findByNamedQueryAndNamedParams("Movie.findByTorrent", params));
	}

	@Override
	public void persist(UserMovie userMovie) {
		em.persist(userMovie);
	}

	@Override
	public void delete(UserMovie userMovie) {
		em.remove(userMovie);
	}

	@Override
	public UserMovie findUserMovie(User user, long movieId) {
		Map<String, Object> params = new HashMap<>(2);
		params.put("movieId", movieId);
		params.put("userId", user.getId());
		return uniqueResult(super.<UserMovie>findByNamedQueryAndNamedParams("UserMovie.findUserMovie", params));
	}

	@Override
	public Movie findByImdbUrl(String imdbUrl) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("imdbUrl", imdbUrl);
		return uniqueResult(super.<Movie>findByNamedQueryAndNamedParams("Movie.findByImdbUrl", params));
	}

	@Override
	public List<UserMovie> findUserMovies(User user, int backlogDays) {
		Map<String, Object> params = new HashMap<>(2);
		params.put("userId", user.getId());
		params.put("downloadDate", DateUtils.getPastDate(backlogDays));
		return super.findByNamedQueryAndNamedParams("UserMovie.findUserMovies", params);
	}

	@Override
	public List<Movie> findAllUserMovies(int backlogDays) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("downloadDate", DateUtils.getPastDate(backlogDays));
		return super.findByNamedQueryAndNamedParams("UserMovie.findAllUserMovies", params);
	}

	@Override
	public int findUserMoviesCount(User user, int backlogDays) {
		Map<String, Object> params = new HashMap<>(2);
		params.put("userId", user.getId());
		params.put("downloadDate", DateUtils.getPastDate(backlogDays));
		return uniqueResult(super.<Long>findByNamedQueryAndNamedParams("UserMovie.findUserMoviesCount", params)).intValue();
	}

	@Override
	public Collection<UserMovie> findUserMoviesByMovieId(long movieId) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("movieId", movieId);
		return super.findByNamedQueryAndNamedParams("UserMovie.findUserMoviesByMovieId", params);
	}

	@Override
	public Collection<UserMovie> findUserMoviesByIMDBIds(User user, Collection<String> imdbIds) {
		if (imdbIds.isEmpty()) {
			return Collections.emptyList();
		}

		Map<String, Object> params = new HashMap<>(2);
		params.put("userId", user.getId());
		params.put("imdbIds", imdbIds);
		return super.findByNamedQueryAndNamedParams("UserMovie.findUserMoviesByIMDBIds", params);
	}

	@Override
	public Collection<User> findUsersForFutureMovie(Movie movie) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("movieId", movie.getId());
		return super.findByNamedQueryAndNamedParams("UserMovie.findUsersForFutureMovie", params);
	}
}
