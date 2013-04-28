package rss.services.searchers;

import rss.services.requests.MediaRequest;
import rss.entities.Media;
import rss.services.searchers.SearchResult;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 14:35
 */
public interface TorrentSearcher<T extends MediaRequest, S extends Media> {

	SearchResult<S> search(T mediaRequest);

	String getName();
}
