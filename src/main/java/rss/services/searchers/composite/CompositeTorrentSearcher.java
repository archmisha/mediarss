package rss.services.searchers.composite;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import rss.entities.Media;
import rss.entities.MediaQuality;
import rss.entities.Torrent;
import rss.services.log.LogService;
import rss.services.requests.MediaRequest;
import rss.services.searchers.SearchResult;
import rss.services.searchers.SimpleTorrentSearcher;
import rss.services.searchers.TorrentSearcher;
import rss.util.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 19:22
 */
public abstract class CompositeTorrentSearcher<T extends MediaRequest, S extends Media> implements TorrentSearcher<T, S> {

	@Autowired
	protected LogService logService;

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public SearchResult search(T mediaRequest) {
		CompositeSearcherData compositeSearcherData = new CompositeSearcherData();

		searchHelper(mediaRequest, compositeSearcherData);

		if (compositeSearcherData.getSuccessfulSearchResult() != null) {
			logTorrentFound(mediaRequest, compositeSearcherData);

			// set the quality in the torrent
			for (Torrent torrent : compositeSearcherData.getSuccessfulSearchResult().getTorrents()) {
				for (MediaQuality mediaQuality : MediaQuality.values()) {
					if ( torrent.getTitle().contains(mediaQuality.toString())) {
						torrent.setQuality(mediaQuality);
						break;
					}
				}
			}

			return compositeSearcherData.getSuccessfulSearchResult();
		}

		return SearchResult.createNotFound();
	}

	protected void searchHelper(T mediaRequest, CompositeSearcherData compositeSearcherData) {
		// then try in the different simple searchers
		for (SimpleTorrentSearcher<T, S> torrentSearcher : getTorrentSearchers()) {
			try {
				SearchResult searchResult = performSearch(mediaRequest, torrentSearcher);
				switch (searchResult.getSearchStatus()) {
					case NOT_FOUND:
						compositeSearcherData.getFailedSearchers().add(torrentSearcher.getName());
						break;
					case FOUND:
						compositeSearcherData.setSuccessfulSearchResult(searchResult); // save a successful result for the end, if searching for IMDB url fails
						// case of no IMDB url
						if (shouldFailOnNoIMDBUrl() && searchResult.getImdbId() == null) {
							compositeSearcherData.getNoIMDBUrlSearchers().add(torrentSearcher.getName());
						} else {
//							logTorrentFound(mediaRequest, compositeSearcherData);
							return;
						}
					case AWAITING_AGING:
						compositeSearcherData.setSuccessfulSearchResult(searchResult);
						return;
				}
			} catch (Exception e) {
				compositeSearcherData.getFailedSearchers().add(torrentSearcher.getName());
				// no need to print the exception stack trace - if its 'Read timed out' error or 'Connect to 1337x.org:80 timed out' error
				if (Utils.isRootCauseMessageContains(e, "timed out")) {
					logService.error(getClass(), e.getMessage());
				} else {
					logService.error(getClass(), e.getMessage(), e);
				}
			}
		}
	}

	protected SearchResult performSearch(T mediaRequest, SimpleTorrentSearcher<T, S> torrentSearcher) {
		return torrentSearcher.search(mediaRequest);
	}

	@SuppressWarnings("unchecked")
	protected Collection<? extends SimpleTorrentSearcher<T, S>> getTorrentSearchers() {
		Collection<SimpleTorrentSearcher<T, S>> searchers = new ArrayList<>();
		for (SimpleTorrentSearcher searcher : applicationContext.getBeansOfType(SimpleTorrentSearcher.class).values()) {
			searchers.add(searcher);
		}
		return searchers;
	}

	protected abstract boolean shouldFailOnNoIMDBUrl();

	protected void logTorrentFound(T mediaRequest, CompositeSearcherData compositeSearcherData) {
		StringBuilder sb = new StringBuilder().append("Found \"").append(mediaRequest.toString())
				.append("\" in ").append(compositeSearcherData.getSuccessfulSearchResult().getSource());
		int counter = 0;
		if (!compositeSearcherData.getFailedSearchers().isEmpty()) {
			sb.append(" (was missing at: ").append(StringUtils.join(compositeSearcherData.getFailedSearchers(), ", "));
			counter++;
		}
		if (!compositeSearcherData.getNoIMDBUrlSearchers().isEmpty()) {
			if (counter > 0) {
				sb.append(", ");
			} else {
				sb.append(" (");
			}
			sb.append("no IMDB url at: ").append(StringUtils.join(compositeSearcherData.getNoIMDBUrlSearchers(), ", "));
			counter++;
		}
		if (counter > 0) {
			sb.append(")");
		}
//		sb.append(" (torrent=").append(compositeSearcherData.getSuccessfulSearchResult().getTorrentTitles()).append(")");
		logService.info(getClass(), sb.toString());
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	protected class CompositeSearcherData {
		private List<String> failedSearchers;
		private List<String> noIMDBUrlSearchers;
		private SearchResult successfulSearchResult;

		public CompositeSearcherData() {
			failedSearchers = new ArrayList<>();
			noIMDBUrlSearchers = new ArrayList<>();
		}

		public List<String> getFailedSearchers() {
			return failedSearchers;
		}

		public List<String> getNoIMDBUrlSearchers() {
			return noIMDBUrlSearchers;
		}

		public SearchResult getSuccessfulSearchResult() {
			return successfulSearchResult;
		}

		public void setSuccessfulSearchResult(SearchResult successfulSearchResult) {
			this.successfulSearchResult = successfulSearchResult;
		}
	}
}
