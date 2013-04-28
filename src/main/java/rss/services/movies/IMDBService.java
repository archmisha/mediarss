package rss.services.movies;

import rss.entities.Movie;

import java.io.InputStream;

/**
 * User: dikmanm
 * Date: 21/04/13 21:54
 */
public interface IMDBService {

	IMDBParseResult downloadMovieFromIMDB(String imdbUrl);

	IMDBParseResult downloadMovieFromIMDBAndImagesAsync(String imdbUrl);

	InputStream getImage(String imageFileName);

	 void downloadImages(String page, String imdbUrl);
}
