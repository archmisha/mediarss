package rss.services.movies;

import rss.entities.Movie;

/**
 * User: dikmanm
 * Date: 20/04/13 15:04
 */
public interface IMDBPreviewCacheService {

	String getImdbPreviewPage(Movie movie);

	String getImdbCSS(String cssFileName);
}
