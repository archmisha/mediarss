package rss.services.searchers.composite;

import com.google.common.primitives.Ints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import rss.PageDownloadException;
import rss.entities.Torrent;
import rss.services.log.LogService;
import rss.services.requests.MediaRequest;
import rss.services.searchers.SearchResult;
import rss.services.searchers.SearcherUtils;
import rss.services.searchers.SimpleTorrentSearcher;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: dikmanm
 * Date: 12/05/13 21:13
 */
public abstract class AbstractCompositeSearcher<T extends MediaRequest> {

	public static final Pattern MAGNET_LINK_HASH_PART_PATTERN = Pattern.compile("btih:([^&]+)&");

	@Autowired
	protected LogService logService;

	@Autowired
	private ApplicationContext applicationContext;

	public SearchResult search(T mediaRequest) {
		Map<String, SearchResult.SearcherFailedReason> failedSearchers = new HashMap<>();
		try {
			preSearch(mediaRequest);

			SearchResult awaitingAgingSearchResult = null;

			for (SimpleTorrentSearcher<T> torrentSearcher : getTorrentSearchers()) {
				try {
					SearchResult searchResult = performSearch(mediaRequest, torrentSearcher);
					setTorrentQuality(searchResult);
					setTorrentHash(searchResult);
					switch (searchResult.getSearchStatus()) {
						case NOT_FOUND:
							failedSearchers.put(torrentSearcher.getName(), SearchResult.SearcherFailedReason.NOT_FOUND);
							break;
						case FOUND:
							// save a successful result for the end, if searching for IMDB url fails
							SearchResult.SearcherFailedReason msg = onTorrentFound(searchResult);
							if (msg == null) {
								searchResult.addFailedSearchers(failedSearchers);
								postSearch(mediaRequest, searchResult);
								return searchResult;
							}

							failedSearchers.put(torrentSearcher.getName(), msg);
							break;
						case AWAITING_AGING:
							awaitingAgingSearchResult = searchResult;
							break;
					}
				} catch (PageDownloadException e) {
					failedSearchers.put(torrentSearcher.getName(), SearchResult.SearcherFailedReason.EXCEPTION);
					logService.error(getClass(), e.getMessage());
				} catch (Exception e) {
					failedSearchers.put(torrentSearcher.getName(), SearchResult.SearcherFailedReason.EXCEPTION);
					logService.error(getClass(), e.getMessage(), e);
				}
			}

			if (awaitingAgingSearchResult != null) {
				awaitingAgingSearchResult.addFailedSearchers(failedSearchers);
				postSearch(mediaRequest, awaitingAgingSearchResult);
				return awaitingAgingSearchResult;
			}
		} catch (Exception e) {
			logService.error(getClass(), e.getMessage(), e);
		}

		SearchResult notFoundSearchResult = SearchResult.createNotFound(failedSearchers);
		postSearch(mediaRequest, notFoundSearchResult);
		return notFoundSearchResult;
	}

	private void setTorrentHash(SearchResult searchResult) {
		// magnet:?xt=urn:btih:8ad5265a9a8b445b26eb1799084edb840192afd7&dn=greys.anatomy.s01e06.avi&tr=udp%3A%2F%2Ftracker.openbittorrent.com%3A80&tr=udp%3A%2F%2Ftracker.publicbt.com%3A80&tr=udp%3A%2F%2Ftracker.istole.it%3A6969&tr=udp%3A%2F%2Ftracker.ccc.de%3A80
		for (Torrent torrent : searchResult.<Torrent>getDownloadables()) {
			if (torrent.getHash() == null) {
				Matcher matcher = MAGNET_LINK_HASH_PART_PATTERN.matcher(torrent.getUrl());
				if (matcher.find()) {
					torrent.setHash(matcher.group(1));
				} else {
					logService.error(getClass(), "Failed extracting hash from magnet: " + torrent.getUrl());
				}
			}
		}
	}

	protected void preSearch(T mediaRequest) {
	}

	protected void postSearch(T mediaRequest, SearchResult searchResult) {
	}

	@SuppressWarnings("unchecked")
	protected Collection<? extends SimpleTorrentSearcher<T>> getTorrentSearchers() {
		List<SimpleTorrentSearcher<T>> searchers = new ArrayList<>();
		for (SimpleTorrentSearcher searcher : applicationContext.getBeansOfType(SimpleTorrentSearcher.class).values()) {
			searchers.add(searcher);
		}

		// sort by preference (kat.ph is always better than 1337x.org)
		Collections.sort(searchers, new Comparator<SimpleTorrentSearcher<T>>() {
			@Override
			public int compare(SimpleTorrentSearcher<T> o1, SimpleTorrentSearcher<T> o2) {
				return Ints.compare(o1.getPriority(), o2.getPriority());
			}
		});
		return searchers;
	}

	private void setTorrentQuality(SearchResult searchResult) {
		for (Torrent torrent : searchResult.<Torrent>getDownloadables()) {
			torrent.setQuality(SearcherUtils.findQuality(torrent.getTitle()));
		}
	}

	protected abstract SearchResult performSearch(T mediaRequest, SimpleTorrentSearcher<T> torrentSearcher);

	protected SearchResult.SearcherFailedReason onTorrentFound(SearchResult searchResult) {
		return null;
	}
}
