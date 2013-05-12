package rss.services.searchers.composite;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import rss.PageDownloadException;
import rss.entities.Media;
import rss.entities.MediaQuality;
import rss.entities.Torrent;
import rss.services.log.LogService;
import rss.services.requests.MediaRequest;
import rss.services.searchers.SearchResult;
import rss.services.searchers.SimpleTorrentSearcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: dikmanm
 * Date: 12/05/13 21:13
 */
public abstract class AbstractCompositeSearcher<T extends MediaRequest> {

	@Autowired
	protected LogService logService;

	@Autowired
	private ApplicationContext applicationContext;

	public SearchResult search(T mediaRequest) {
		try {
			prepareSearchRequest(mediaRequest);

			CompositeSearcherData compositeSearcherData = new CompositeSearcherData();

			for (SimpleTorrentSearcher<T, Media> torrentSearcher : getTorrentSearchers()) {
				if (searchAndAnalyze(torrentSearcher, mediaRequest, compositeSearcherData)) {
					break;
				}
			}

			if (compositeSearcherData.getSuccessfulSearchResult() != null) {
				logTorrentFound(mediaRequest, compositeSearcherData);
				return compositeSearcherData.getSuccessfulSearchResult();
			}
		} catch (Exception e) {
			logService.error(getClass(), e.getMessage(), e);
		}

		return SearchResult.createNotFound();
	}

	protected void prepareSearchRequest(T mediaRequest) {
	}

	@SuppressWarnings("unchecked")
	protected Collection<? extends SimpleTorrentSearcher<T, Media>> getTorrentSearchers() {
		Collection<SimpleTorrentSearcher<T, Media>> searchers = new ArrayList<>();
		for (SimpleTorrentSearcher searcher : applicationContext.getBeansOfType(SimpleTorrentSearcher.class).values()) {
			searchers.add(searcher);
		}
		return searchers;
	}

	private boolean searchAndAnalyze(SimpleTorrentSearcher<T, Media> torrentSearcher,
									 T mediaRequest,
									 CompositeSearcherData compositeSearcherData) {
		try {
			SearchResult searchResult = performSearch(mediaRequest, torrentSearcher);
			switch (searchResult.getSearchStatus()) {
				case NOT_FOUND:
					compositeSearcherData.getFailedSearchers().add(torrentSearcher.getName());
					break;
				case FOUND:
					// save a successful result for the end, if searching for IMDB url fails
					compositeSearcherData.setSuccessfulSearchResult(searchResult);
					setTorrentQuality(compositeSearcherData);
					onTorrentFound(compositeSearcherData, searchResult, torrentSearcher.getName());
					return true;
				case AWAITING_AGING:
					compositeSearcherData.setSuccessfulSearchResult(searchResult);
					setTorrentQuality(compositeSearcherData);
					return true;
			}
		} catch (PageDownloadException e) {
			compositeSearcherData.getFailedSearchers().add(torrentSearcher.getName());
			logService.error(getClass(), e.getMessage());
		} catch (Exception e) {
			compositeSearcherData.getFailedSearchers().add(torrentSearcher.getName());
			logService.error(getClass(), e.getMessage(), e);
		}
		return false;
	}

	private void setTorrentQuality(CompositeSearcherData compositeSearcherData) {
		// set the quality in the torrent
		for (Torrent torrent : compositeSearcherData.getSuccessfulSearchResult().<Torrent>getDownloadables()) {
			for (MediaQuality mediaQuality : MediaQuality.values()) {
				if (torrent.getTitle().toLowerCase().contains(mediaQuality.toString())) {
					torrent.setQuality(mediaQuality);
					break;
				}
			}
		}
	}

	protected abstract SearchResult performSearch(T mediaRequest, SimpleTorrentSearcher<T, Media> torrentSearcher);

	protected void onTorrentFound(CompositeSearcherData compositeSearcherData, SearchResult searchResult,
										   String searcherName) {}

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
