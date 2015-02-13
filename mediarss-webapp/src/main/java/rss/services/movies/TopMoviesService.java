package rss.services.movies;

import rss.entities.Movie;

import java.util.Collection;

/**
 * User: dikmanm
 * Date: 20/02/14 17:48
 */
public interface TopMoviesService {

	void downloadTopMovies();

	Collection<Movie> getTopMovies();
}
