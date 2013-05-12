package rss.services.searchers;

import rss.entities.Media;
import rss.services.requests.MediaRequest;
import rss.services.searchers.composite.DefaultCompositeSearcher;
import rss.services.searchers.composite.torrentz.TorrentzSearcher;

/**
 * User: dikmanm
 * Date: 12/05/13 21:03
 */
public abstract class AbstractMediaSearcher<T extends MediaRequest, S extends Media> implements Searcher<T, S> {

	@Override
	public SearchResult search(T mediaRequest) {
		SearchResult searchResult = getTorrentzSearcher().search(mediaRequest);
		if (searchResult.getSearchStatus() == SearchResult.SearchStatus.NOT_FOUND) {
			searchResult = getDefaultCompositeSearcher().search(mediaRequest);
		}
		return searchResult;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	protected abstract DefaultCompositeSearcher<T> getDefaultCompositeSearcher();

	protected abstract TorrentzSearcher<T> getTorrentzSearcher();
}
