package rss.movies.imdb;

import rss.torrents.Movie;

/**
 * User: dikmanm
 * Date: 20/04/13 15:04
 */
public interface IMDBPreviewCacheService {

	void addImdbPreview(Movie movie, String page);

	String getImdbPreviewPage(Movie movie);

	String getImdbCSS(String cssFileName);
}
