package rss.torrents.searchers;

import rss.torrents.requests.SearchRequest;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 14:35
 */
public interface Searcher<T extends SearchRequest> {

	String getName();

	SearchResult search(T mediaRequest);
}
