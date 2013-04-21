package rss.services.movies;

import rss.entities.Movie;

/**
 * User: dikmanm
 * Date: 21/04/13 21:54
 */
public interface IMDBService {

	int extractMovieYear(Movie movie);

	IMDBParseResult downloadMovieFromIMDB(String imdbUrl);
}
