package rss.services.searchers;

import org.springframework.beans.factory.annotation.Autowired;
import rss.services.requests.MediaRequest;
import rss.services.searchers.composite.DefaultCompositeSearcher;
import rss.services.searchers.composite.torrentz.TorrentzSearcher;

import java.util.Map;

/**
 * User: dikmanm
 * Date: 12/05/13 21:03
 */
public abstract class AbstractMediaSearcher<T extends MediaRequest> implements Searcher<T> {

    @Autowired
    private SearcherConfigurationService searcherConfigurationService;

    @Override
    public SearchResult search(T mediaRequest) {
        SearchResult searchResult;
        if (searcherConfigurationService.torrentzIsEnabled()) {
            searchResult = getTorrentzSearcher().search(mediaRequest);
            if (searchResult.getSearchStatus() == SearchResult.SearchStatus.NOT_FOUND) {
                Map<String, SearchResult.SearcherFailedReason> failedSearchers = searchResult.getFailedSearchers();
                searchResult = getDefaultCompositeSearcher().search(mediaRequest);
                searchResult.addFailedSearchers(failedSearchers);
            }
        } else {
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
