package rss.services.searchers;

import rss.services.requests.MediaRequest;
import rss.entities.Media;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 14:35
 */
public interface TorrentSearcher<T extends MediaRequest, S extends Media> {

	String getName();

	SearchResult search(T mediaRequest);
}
