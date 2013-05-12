package rss.services.searchers;

import rss.entities.Media;
import rss.services.requests.SearchRequest;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 14:35
 */
public interface Searcher<T extends SearchRequest, S extends Media> {

	String getName();

	SearchResult search(T mediaRequest);
}
