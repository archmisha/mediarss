package rss.movies.imdb;

import java.io.InputStream;
import java.util.Collection;

/**
 * User: dikmanm
 * Date: 21/04/13 21:54
 */
public interface IMDBService {

	IMDBParseResult downloadMovieFromIMDB(String imdbUrl);

	IMDBParseResult downloadMovieFromIMDBAndImagesAsync(String imdbUrl);

	InputStream getPersonImage(String imageFileName);

	InputStream getMovieImage(String imageFileName);

	void downloadImages(String page, String imdbUrl);

	Collection<IMDBAutoCompleteItem> search(String query);
}
