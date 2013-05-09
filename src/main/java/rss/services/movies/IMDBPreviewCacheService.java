package rss.services.movies;

import rss.entities.Movie;

/**
 * User: dikmanm
 * Date: 20/04/13 15:04
 */
public interface IMDBPreviewCacheService {

	String cleanImdbPage(String name, String page);

	String getImdbPreviewPage(Movie movie);

	String getImdbCSS(String cssFileName);
}
