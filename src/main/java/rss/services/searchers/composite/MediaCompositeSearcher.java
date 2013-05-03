package rss.services.searchers.composite;

import rss.entities.Media;
import rss.services.requests.MediaRequest;
import rss.services.searchers.SearchResult;
import rss.util.Utils;

/**
 * User: dikmanm
 * Date: 30/04/13 21:59
 */
public abstract class MediaCompositeSearcher<T extends MediaRequest, S extends Media> extends CompositeTorrentSearcher<T, S> {

	@Override
	protected void searchHelper(T mediaRequest, CompositeSearcherData compositeSearcherData) {
		// first try in torrentzSearcher
		CompositeTorrentSearcher<T, S> torrentzSearcher = getTorrentzSearcher();
		try {
			SearchResult searchResult = torrentzSearcher.search(mediaRequest);
			switch (searchResult.getSearchStatus()) {
				case NOT_FOUND:
					compositeSearcherData.getFailedSearchers().add(torrentzSearcher.getName());
					break;
				case FOUND:
					compositeSearcherData.setSuccessfulSearchResult(searchResult); // save a successful result for the end, if searching for IMDB url fails
					// case of no IMDB url
					if (shouldFailOnNoIMDBUrl() && searchResult.getImdbId() == null) {
						compositeSearcherData.getNoIMDBUrlSearchers().add(torrentzSearcher.getName());
					} else {
						// dont log, super will log
//						logTorrentFound(mediaRequest, compositeSearcherData);
						return;
					}
				case AWAITING_AGING:
					compositeSearcherData.setSuccessfulSearchResult(searchResult);
					return;
			}
		} catch (Exception e) {
			compositeSearcherData.getFailedSearchers().add(torrentzSearcher.getName());
			// no need to print the exception stack trace - if its 'Read timed out' error or 'Connect to 1337x.org:80 timed out' error
			if (Utils.isRootCauseMessageContains(e, "timed out")) {
				logService.error(getClass(), e.getMessage());
			} else {
				logService.error(getClass(), e.getMessage(), e);
			}
		}

		if (compositeSearcherData.getSuccessfulSearchResult() != null) {
//			logTorrentFound(mediaRequest, compositeSearcherData);
			return;
		}

		super.searchHelper(mediaRequest, compositeSearcherData);
	}

	protected abstract CompositeTorrentSearcher<T, S> getTorrentzSearcher();
}
