package rss.services.searchers.composite;

import org.springframework.stereotype.Service;
import rss.services.searchers.SearchResult;
import rss.services.searchers.SimpleTorrentSearcher;
import rss.torrents.requests.MediaRequest;

/**
 * User: dikmanm
 * Date: 12/05/13 21:20
 */
@Service("defaultCompositeSearcher")
public class DefaultCompositeSearcher<T extends MediaRequest> extends AbstractCompositeSearcher<T> {

	@Override
	protected SearchResult performSearch(T mediaRequest, SimpleTorrentSearcher<T> torrentSearcher) {
		return torrentSearcher.search(mediaRequest);
	}
}
