package rss.torrents.searchers.composite;

import org.springframework.stereotype.Service;
import rss.torrents.requests.MediaRequest;
import rss.torrents.searchers.SearchResult;
import rss.torrents.searchers.SimpleTorrentSearcher;

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
