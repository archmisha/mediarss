package rss.services.searchers.composite;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import rss.PageDownloadException;
import rss.entities.Media;
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
		try {
			prepareSearchRequest(mediaRequest);

			List<Pair<String, String>> failedSearchers = new ArrayList<>();
			SearchResult awaitingAgingSearchResult = null;

			for (SimpleTorrentSearcher<T, Media> torrentSearcher : getTorrentSearchers()) {
				try {
					SearchResult searchResult = performSearch(mediaRequest, torrentSearcher);
					setTorrentQuality(searchResult);
					setTorrentHash(searchResult);
					switch (searchResult.getSearchStatus()) {
						case NOT_FOUND:
							failedSearchers.add(new ImmutablePair<>(torrentSearcher.getName(), "not found"));
							break;
						case FOUND:
							// save a successful result for the end, if searching for IMDB url fails
							String msg = onTorrentFound(searchResult);
							if (msg == null) {
								logTorrentFound(mediaRequest, searchResult, failedSearchers);
								return searchResult;
							}

							failedSearchers.add(new ImmutablePair<>(torrentSearcher.getName(), msg));
							break;
						case AWAITING_AGING:
							awaitingAgingSearchResult = searchResult;
							break;
					}
				} catch (PageDownloadException e) {
					failedSearchers.add(new ImmutablePair<>(torrentSearcher.getName(), "exception"));
					logService.error(getClass(), e.getMessage());
				} catch (Exception e) {
					failedSearchers.add(new ImmutablePair<>(torrentSearcher.getName(), "exception"));
					logService.error(getClass(), e.getMessage(), e);
				}
			}

			if (awaitingAgingSearchResult != null) {
				logTorrentFound(mediaRequest, awaitingAgingSearchResult, failedSearchers);
				return awaitingAgingSearchResult;
			}
		} catch (Exception e) {
			logService.error(getClass(), e.getMessage(), e);
		}

		return SearchResult.createNotFound();
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

	protected void prepareSearchRequest(T mediaRequest) {
	}

	@SuppressWarnings("unchecked")
	protected Collection<? extends SimpleTorrentSearcher<T, Media>> getTorrentSearchers() {
		List<SimpleTorrentSearcher<T, Media>> searchers = new ArrayList<>();
		for (SimpleTorrentSearcher searcher : applicationContext.getBeansOfType(SimpleTorrentSearcher.class).values()) {
			searchers.add(searcher);
		}

		// sort by preference (kat.ph is always better than 1337x.org)
		Collections.sort(searchers, new Comparator<SimpleTorrentSearcher<T, Media>>() {
			@Override
			public int compare(SimpleTorrentSearcher<T, Media> o1, SimpleTorrentSearcher<T, Media> o2) {
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

	protected abstract SearchResult performSearch(T mediaRequest, SimpleTorrentSearcher<T, Media> torrentSearcher);

	protected String onTorrentFound(SearchResult searchResult) {
		return null;
	}

	protected void logTorrentFound(T mediaRequest, SearchResult searchResult, List<Pair<String, String>> failedSearchers) {
		StringBuilder sb = new StringBuilder().append("Found \"").append(mediaRequest.toString()).append("\" in ").append(searchResult.getSource());
		if (!failedSearchers.isEmpty()) {
			sb.append(" (was missing at: ");
			for (Pair<String, String> pair : failedSearchers) {
				sb.append(pair.getKey()).append(" - ").append(pair.getValue()).append(", ");
			}
			sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1).append(")");
		}
		logService.info(getClass(), sb.toString());
	}
}
