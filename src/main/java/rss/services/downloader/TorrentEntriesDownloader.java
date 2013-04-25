package rss.services.downloader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.entities.Media;
import rss.entities.Torrent;
import rss.services.SearchResult;
import rss.services.log.LogService;
import rss.services.requests.MediaRequest;
import rss.util.MultiThreadExecutor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: Michael Dikman
 * Date: 02/12/12
 * Time: 23:49
 */
public abstract class TorrentEntriesDownloader<T extends Media, S extends MediaRequest> {

	public static final int MAX_CONCURRENT_EPISODES = 15;

	@Autowired
	protected LogService logService;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public DownloadResult<T, S> download(Collection<S> mediaRequests) {
		return download(mediaRequests, Executors.newFixedThreadPool(MAX_CONCURRENT_EPISODES), false);
	}

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public DownloadResult<T, S> download(Collection<S> mediaRequests, boolean forceDownload) {
		return download(mediaRequests, Executors.newFixedThreadPool(MAX_CONCURRENT_EPISODES), forceDownload);
	}

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public DownloadResult<T, S> download(Collection<S> mediaRequests, ExecutorService executorService, boolean forceDownload) {
		// copying to avoid UnsupportedOperationException if immutable collections is given
		final Set<S> mediaRequestsCopy = new HashSet<>(mediaRequests);

		// enriching the set before the cache query - maybe expanding full season request into parts
		// modifying and enriching the set inside the method
		// first query the cache and those that are not found in cache divide between the threads
		Collection<T> cachedTorrentEntries = preDownloadPhase(mediaRequestsCopy, forceDownload);

		final ConcurrentLinkedQueue<T> result = new ConcurrentLinkedQueue<>();
		final ConcurrentLinkedQueue<S> missing = new ConcurrentLinkedQueue<>();
		final Class aClass = getClass();
		MultiThreadExecutor.execute(executorService, mediaRequestsCopy, logService, new MultiThreadExecutor.MultiThreadExecutorTask<S>() {
			@Override
			public void run(final S mediaRequest) {
				try {
					final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm");
					final long from = System.currentTimeMillis();
					final SearchResult<T> searchResult = downloadTorrent(mediaRequest);

					transactionTemplate.execute(new TransactionCallbackWithoutResult() {
						@Override
						protected void doInTransactionWithoutResult(TransactionStatus arg0) {
							Torrent searchResultTorrent = searchResult.getTorrent();
							switch (searchResult.getSearchStatus()) {
								case NOT_FOUND:
									onTorrentMissing(mediaRequest, searchResult);
									logService.info(aClass, String.format("Media \"%s\" is not found. Took %d millis.",
											mediaRequest.toString(), // searchResultTorrent and media doesn't have torrentEntry in that case
											System.currentTimeMillis() - from));
									missing.add(mediaRequest);
									break;
								case AWAITING_AGING:
									logService.info(aClass, String.format("Torrent \"%s\" is not yet passed aging, uploaded on %s. Took %d millis.",
											searchResultTorrent.getTitle(),
											DATE_FORMAT.format(searchResultTorrent.getDateUploaded()),
											System.currentTimeMillis() - from));
									// do nothing - its not missing cuz no need to  email and not found
									break;
								case FOUND:
									List<T> mediaList = onTorrentFound(mediaRequest, searchResult);
									if (!mediaList.isEmpty()) {
										// printing the returned torrent and not the original , as it might undergone some transformations
										logService.info(aClass, String.format("Downloading \"%s\" took %d millis. Found in %s",
												searchResultTorrent.getTitle(),
												System.currentTimeMillis() - from,
												searchResult.getSource()));
										result.addAll(mediaList);
									}
									break;
							}
						}
					});
				} catch (Exception e) {
					logService.error(aClass, "Failed retrieving \"" + mediaRequest.toString() + "\": " + e.getMessage(), e);
				}
			}
		});


		// add cached torrents to the list
		result.addAll(cachedTorrentEntries);

		return new DownloadResult<>(result, missing);
	}

	protected abstract void onTorrentMissing(S mediaRequest, SearchResult<T> searchResult);

	protected abstract Collection<T> preDownloadPhase(Set<S> mediaRequestsCopy, boolean forceDownload);

	protected abstract List<T> onTorrentFound(S mediaRequest, SearchResult<T> searchResult);

	protected abstract SearchResult<T> downloadTorrent(S request);
}
