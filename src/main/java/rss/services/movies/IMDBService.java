package rss.services.movies;

import rss.entities.Movie;

/**
 * User: dikmanm
 * Date: 21/04/13 21:54
 */
public interface IMDBService {

	IMDBParseResult downloadMovieFromIMDB(String imdbUrl);
}
