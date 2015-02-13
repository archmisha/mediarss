package rss.services.downloader;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.PageDownloadException;
import rss.services.log.LogService;
import rss.services.requests.SearchRequest;
import rss.services.searchers.Downloadable;
import rss.services.searchers.SearchResult;
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
public abstract class BaseDownloader<S extends SearchRequest, T> {

    public static final int MAX_CONCURRENT_REQUESTS = 15;

    @Autowired
    protected LogService logService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Transactional(propagation = Propagation.REQUIRED)
    public DownloadResult<T, S> download(Set<S> mediaRequests, DownloadConfig downloadConfig) {
        return download(mediaRequests, Executors.newFixedThreadPool(MAX_CONCURRENT_REQUESTS), downloadConfig);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public DownloadResult<T, S> download(final Set<S> mediaRequests,
                                         final ExecutorService executorService,
                                         DownloadConfig downloadConfig) {
        // enriching the set before the cache query - maybe expanding full season request into parts
        // modifying and enriching the set inside the method
        // first query the cache and those that are not found in cache divide between the threads
        final Collection<T> cachedTorrentEntries;
        try {
            cachedTorrentEntries = preDownloadPhase(mediaRequests, downloadConfig.isForceDownload());
        } catch (Exception e) {
            executorService.shutdown();
            throw e;
        }

        // if this is might be a heavy download and we Don't have everything in cache - make it async
        if (downloadConfig.isAsyncHeavy() && !mediaRequests.isEmpty()) {
            final DownloadResult<T, S> downloadResult = DownloadResult.createHeavyDownloadResult();
            ExecutorService es = Executors.newSingleThreadExecutor();
            es.submit(new Runnable() {
                @Override
                public void run() {
                    downloadHelper(executorService, mediaRequests, cachedTorrentEntries, downloadResult);
                }
            });
            es.shutdown();
            return downloadResult;
        } else {
            return downloadHelper(executorService, mediaRequests, cachedTorrentEntries, DownloadResult.<T, S>createLightDownloadResult());
        }
    }

    private DownloadResult<T, S> downloadHelper(ExecutorService executorService,
                                                Set<S> mediaRequestsCopy,
                                                Collection<T> cachedTorrentEntries,
                                                DownloadResult<T, S> downloadResult) {
        final Class aClass = getClass();
        final ConcurrentLinkedQueue<Pair<S, SearchResult>> notProcessedResults = new ConcurrentLinkedQueue<>();
        final ConcurrentLinkedQueue<T> processedResults = new ConcurrentLinkedQueue<>();
        final ConcurrentLinkedQueue<S> missing = new ConcurrentLinkedQueue<>();

        MultiThreadExecutor.execute(executorService, mediaRequestsCopy, new MultiThreadExecutor.MultiThreadExecutorTask<S>() {
            @Override
            public void run(final S mediaRequest) {
                try {
                    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                        @Override
                        protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                            final long from = System.currentTimeMillis();
                            final SearchResult searchResult = downloadTorrent(mediaRequest);
                            switch (searchResult.getSearchStatus()) {
                                case NOT_FOUND:
                                    logService.info(aClass, String.format("Downloading \"%s\" took %d ms. %s",
                                            mediaRequest.toString(), // searchResultTorrent and media doesn't have torrentEntry in that case
                                            System.currentTimeMillis() - from,
                                            getFoundInPart(searchResult)));

                                    if (isSingleTransaction()) {
                                        missing.add(mediaRequest);
                                    } else {
                                        processSingleMissingRequest(mediaRequest);
                                    }
                                    break;

                                case AWAITING_AGING:
                                    // should be only one of those here
                                    final Downloadable searchResultTorrent = searchResult.getDownloadables().get(0);
                                    final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                                    logService.info(aClass, String.format("Torrent \"%s\" is not yet passed aging, uploaded on %s. Took %d ms.",
                                            searchResultTorrent.getName(),
                                            DATE_FORMAT.format(searchResultTorrent.getDateUploaded()),
                                            System.currentTimeMillis() - from));
                                    // do nothing - its not missing cuz no need to  email and not found
                                    break;
                                case FOUND:
                                    if (validateSearchResult(mediaRequest, searchResult)) {
                                        // printing the returned torrent and not the original , as it might undergone some transformations
                                        logService.info(aClass, String.format("Downloading \"%s\" took %d ms. %s",
                                                searchResult.getDownloadablesDisplayString(),
                                                System.currentTimeMillis() - from,
                                                getFoundInPart(searchResult)));

                                        if (isSingleTransaction()) {
                                            notProcessedResults.add(new ImmutablePair<>(mediaRequest, searchResult));
                                        } else {
                                            processedResults.addAll(processSingleSearchResult(mediaRequest, searchResult));
                                        }
                                    }
                                    break;
                            }
                        }
                    });
                } catch (PageDownloadException e) {
                    logService.error(aClass, String.format("Failed retrieving \"%s\": %s", mediaRequest, e.getMessage()));
                } catch (Exception e) {
                    logService.error(aClass, String.format("Failed retrieving \"%s\": %s", mediaRequest, e.getMessage()), e);
                }
            }
        });

        Collection<T> result = new ArrayList<>(processedResults);

        // add cached torrents to the list
        result.addAll(cachedTorrentEntries);

        if (!notProcessedResults.isEmpty()) {
            result.addAll(processSearchResults(notProcessedResults));
        }

        if (!missing.isEmpty()) {
            processMissingRequests(missing);
        }

        downloadResult.setDownloaded(result);
        downloadResult.setMissing(missing);
        downloadResult.setCompleteDate(new Date());
        return downloadResult;
    }

    protected abstract boolean isSingleTransaction();

    protected abstract void processSingleMissingRequest(S missing);

    protected abstract void processMissingRequests(Collection<S> missing);

    protected abstract Collection<T> preDownloadPhase(Set<S> mediaRequestsCopy, boolean forceDownload);

    protected abstract boolean validateSearchResult(S mediaRequest, SearchResult searchResult);

    protected abstract Collection<T> processSingleSearchResult(S mediaRequest, SearchResult searchResult);

    protected abstract List<T> processSearchResults(Collection<Pair<S, SearchResult>> results);

    protected abstract SearchResult downloadTorrent(S request);

    private String getFoundInPart(SearchResult searchResult) {
        StringBuilder sb = new StringBuilder();
        if (searchResult.getSearchStatus() != SearchResult.SearchStatus.NOT_FOUND) {
            sb.append("Found in ").append(searchResult.getSourcesDisplayString());
            if (!searchResult.getFailedSearchers().isEmpty()) {
                sb.append(" (was missing at: ");
            }
        } else {
            sb.append("Not found in: ");
        }

        sb.append(searchResult.getFailedSearchersDisplayString());

        if (searchResult.getSearchStatus() != SearchResult.SearchStatus.NOT_FOUND && !searchResult.getFailedSearchers().isEmpty()) {
            sb.append(")");
        }
        return sb.toString();
    }
}
