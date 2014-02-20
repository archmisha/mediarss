package rss.services.movies;

import rss.entities.Movie;

import java.util.HashSet;
import java.util.Set;

/**
 * User: dikmanm
 * Date: 19/02/14 07:41
 */
public class MovieFoneTopMoviesDownloader implements TopMoviesDownloader {

	@Override
	public Set<Movie> getTopMovies(int count) {
		return new HashSet<>();
	}
}
