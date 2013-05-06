package rss.services.downloader;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.entities.Media;
import rss.entities.Torrent;
import rss.services.searchers.SearchResult;
import rss.services.log.LogService;
import rss.services.requests.MediaRequest;
import rss.util.MultiThreadExecutor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: Michael Dikman
 * Date: 02/12/12
 * Time: 23:49
 */
public abstract class TorrentEntriesDownloader<S extends MediaRequest, T extends Media> {

	public static final int MAX_CONCURRENT_REQUESTS = 15;

	@Autowired
	protected LogService logService;

	@Transactional(propagation = Propagation.REQUIRED)
	public DownloadResult<T, S> download(Collection<S> mediaRequests) {
		return download(mediaRequests, Executors.newFixedThreadPool(MAX_CONCURRENT_REQUESTS), false);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public DownloadResult<T, S> download(Collection<S> mediaRequests, boolean forceDownload) {
		return download(mediaRequests, Executors.newFixedThreadPool(MAX_CONCURRENT_REQUESTS), forceDownload);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public DownloadResult<T, S> download(Collection<S> mediaRequests, ExecutorService executorService, boolean forceDownload) {
		// copying to avoid UnsupportedOperationException if immutable collections is given
		final Set<S> mediaRequestsCopy = new HashSet<>(mediaRequests);

		final ConcurrentLinkedQueue<Pair<S, SearchResult>> results = new ConcurrentLinkedQueue<>();
		final ConcurrentLinkedQueue<S> missing = new ConcurrentLinkedQueue<>();
		final Class aClass = getClass();

		// enriching the set before the cache query - maybe expanding full season request into parts
		// modifying and enriching the set inside the method
		// first query the cache and those that are not found in cache divide between the threads
		Collection<T> cachedTorrentEntries = null;
		try {
			cachedTorrentEntries = preDownloadPhase(mediaRequestsCopy, forceDownload);
		} catch (Exception e) {
			executorService.shutdown();
			throw e;
		}

		MultiThreadExecutor.execute(executorService, mediaRequestsCopy, new MultiThreadExecutor.MultiThreadExecutorTask<S>() {
			@Override
			public void run(final S mediaRequest) {
				try {
					final long from = System.currentTimeMillis();
					final SearchResult searchResult = downloadTorrent(mediaRequest);
					switch (searchResult.getSearchStatus()) {
						case NOT_FOUND:
							logService.info(aClass, String.format("Media \"%s\" is not found. Took %d millis.",
									mediaRequest.toString(), // searchResultTorrent and media doesn't have torrentEntry in that case
									System.currentTimeMillis() - from));
							missing.add(mediaRequest);
							break;
						case AWAITING_AGING:
							// should be only one of those here
							final Torrent searchResultTorrent = searchResult.getTorrents().get(0);
							final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm");
							logService.info(aClass, String.format("Torrent \"%s\" is not yet passed aging, uploaded on %s. Took %d millis.",
									searchResultTorrent.getTitle(),
									DATE_FORMAT.format(searchResultTorrent.getDateUploaded()),
									System.currentTimeMillis() - from));
							// do nothing - its not missing cuz no need to  email and not found
							break;
						case FOUND:
							if (validateSearchResult(mediaRequest, searchResult)) {
								// printing the returned torrent and not the original , as it might undergone some transformations
								logService.info(aClass, String.format("Downloading \"%s\" took %d millis. Found in %s",
										searchResult.getTorrentTitles(),
										System.currentTimeMillis() - from,
										searchResult.getSource()));
								results.add(new ImmutablePair<>(mediaRequest, searchResult));
							}
							break;
					}
				} catch (Exception e) {
					logService.error(aClass, String.format("Failed retrieving \"%s\": %s", mediaRequest, e.getMessage()), e);
				}
			}
		});

		Collection<T> result = new ArrayList<>();

		result.addAll(processSearchResults(results));

		// add cached torrents to the list
		result.addAll(cachedTorrentEntries);

		processMissingRequests(missing);

		return new DownloadResult<>(result, missing);
	}

	protected abstract void processMissingRequests(Collection<S> missing);

	protected abstract Collection<T> preDownloadPhase(Set<S> mediaRequestsCopy, boolean forceDownload);

	protected abstract boolean validateSearchResult(S mediaRequest, SearchResult searchResult);

	protected abstract List<T> processSearchResults(Collection<Pair<S, SearchResult>> results);

	protected abstract SearchResult downloadTorrent(S request);
}
