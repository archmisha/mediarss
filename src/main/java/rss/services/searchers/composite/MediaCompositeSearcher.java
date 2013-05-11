package rss.services.searchers.composite;

import rss.PageDownloadException;
import rss.entities.Media;
import rss.services.requests.MediaRequest;
import rss.services.searchers.SearchResult;

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
		} catch (PageDownloadException e) {
			compositeSearcherData.getFailedSearchers().add(torrentzSearcher.getName());
			logService.error(getClass(), e.getMessage());
		} catch (Exception e) {
			compositeSearcherData.getFailedSearchers().add(torrentzSearcher.getName());
			logService.error(getClass(), e.getMessage(), e);
		}

		if (compositeSearcherData.getSuccessfulSearchResult() != null) {
//			logTorrentFound(mediaRequest, compositeSearcherData);
			return;
		}

		super.searchHelper(mediaRequest, compositeSearcherData);
	}

	protected abstract CompositeTorrentSearcher<T, S> getTorrentzSearcher();
}
