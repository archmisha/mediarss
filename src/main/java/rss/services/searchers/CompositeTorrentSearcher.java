package rss.services.searchers;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.entities.Media;
import rss.services.requests.MediaRequest;
import rss.services.SearchResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 19:22
 */
public abstract class CompositeTorrentSearcher implements TorrentSearcher<MediaRequest, Media> {

	private static Log log = LogFactory.getLog(CompositeTorrentSearcher.class);

	// episodeSearcher1337x is slower cuz need to download 2 pages instead of one
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public SearchResult<Media> search(MediaRequest mediaRequest) {
		List<String> failedSearchers = new ArrayList<>();
		List<String> noIMDBUrlSearchers = new ArrayList<>();
		SearchResult<Media> successfulSearchResult = null;
		for (TorrentSearcher<MediaRequest, Media> torrentSearcher : getTorrentSearchers()) {
			try {
				SearchResult<Media> searchResult = torrentSearcher.search(mediaRequest);
				switch (searchResult.getSearchStatus()) {
					case NOT_FOUND:
						failedSearchers.add(torrentSearcher.getName());
						break;
					case FOUND:
						successfulSearchResult = searchResult; // save a successful result for the end, if searching for IMDB url fails
						// case of no IMDB url
						if (shouldFailOnNoIMDBUrl() && searchResult.getMetaData().getImdbUrl() == null) {
							noIMDBUrlSearchers.add(torrentSearcher.getName());
							continue;
						}

						logTorrentFound(mediaRequest, failedSearchers, noIMDBUrlSearchers, searchResult);
						return searchResult;
					case AWAITING_AGING:
						return searchResult;
				}
			} catch (Exception e) {
				failedSearchers.add(torrentSearcher.getName());
				// no need to print the exception stack trace - if its 'Read timed out' error
				if (e.getMessage().contains("Read timed out")) {
					log.error(e.getMessage());
				} else {
					log.error(e.getMessage(), e);
				}
			}
		}

		if (successfulSearchResult != null) {
			logTorrentFound(mediaRequest, failedSearchers, noIMDBUrlSearchers, successfulSearchResult);
			return successfulSearchResult;
		}

		return new SearchResult<>(SearchResult.SearchStatus.NOT_FOUND);
	}

	protected abstract boolean shouldFailOnNoIMDBUrl();

	private void logTorrentFound(MediaRequest mediaRequest, List<String> failedSearchers, List<String> noIMDBUrlSearchers, SearchResult<Media> searchResult) {
		StringBuilder sb = new StringBuilder().append("Found \"").append(mediaRequest.toString())
				.append("\" in ").append(searchResult.getSource());
		int counter = 0;
		if (!failedSearchers.isEmpty()) {
			sb.append(" (was missing at: ").append(StringUtils.join(failedSearchers, ", "));
			counter++;
		}
		if (!noIMDBUrlSearchers.isEmpty()) {
			if (counter > 0) {
				sb.append(", ");
			} else {
				sb.append(" (");
			}
			sb.append("no IMDB url at: ").append(StringUtils.join(noIMDBUrlSearchers, ", "));
			counter++;
		}
		if (counter > 0) {
			sb.append(")");
		}
		sb.append(" (torrent=").append(searchResult.getTorrent().getTitle()).append(")");
		log.info(sb.toString());
	}

	protected abstract Collection<? extends TorrentSearcher<MediaRequest, Media>> getTorrentSearchers();

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}
}
