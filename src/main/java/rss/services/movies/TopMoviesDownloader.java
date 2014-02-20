package rss.services.movies;

import rss.entities.Movie;

import java.util.Set;

/**
 * User: dikmanm
 * Date: 19/02/14 07:41
 */
public interface TopMoviesDownloader {

	Set<Movie> getTopMovies(int count);
}
