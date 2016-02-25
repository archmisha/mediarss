package rss.torrents.downloader;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.shows.CachedShow;
import rss.shows.ShowSearchService;
import rss.shows.ShowService;
import rss.subtitles.SubtitlesScanHistory;
import rss.subtitles.SubtitlesService;
import rss.torrents.Show;
import rss.torrents.Subtitles;
import rss.torrents.requests.subtitles.SubtitlesEpisodeRequest;
import rss.torrents.requests.subtitles.SubtitlesRequest;
import rss.torrents.searchers.SearchResult;
import rss.torrents.searchers.SubCenterSubtitlesSearcher;
import rss.user.subtitles.SubtitleLanguage;
import rss.util.DateUtils;

import java.util.*;

/**
 * User: dikmanm
 * Date: 11/05/13 15:21
 */
@Service
public class SubtitlesDownloaderImpl extends BaseDownloader<SubtitlesRequest, Subtitles> implements SubtitlesDownloader {

    @Autowired
    private SubtitlesService subtitlesService;

    @Autowired
    private ShowSearchService showSearchService;

//	@Autowired
//	private SubtitlesTrackerService subtitlesTrackerService;

    @Autowired
    private SubCenterSubtitlesSearcher subCenterSubtitlesSearcher;

    @Autowired
    private ShowService showService;

    @Override
    protected void processSingleMissingRequest(SubtitlesRequest missing) {
        updateScanDate(missing);
        updateSubcenterOnShow(missing);
    }

    @Override
    protected void processMissingRequests(Collection<SubtitlesRequest> missing) {
        throw new UnsupportedOperationException();
//		for (SubtitlesRequest subtitlesRequest : missing) {
//			updateScanDate(subtitlesRequest);
//
//			if (subtitlesRequest instanceof SubtitlesEpisodeRequest) {
//				SubtitlesEpisodeRequest ser = (SubtitlesEpisodeRequest) subtitlesRequest;
//				update subCenter url of the show, in the request episode it must not be null, otherwise couldn't find the results
//				Show show = showDao.find(ser.getShow().getId());
//				show.setSubCenterUrl(ser.getShow().getSubCenterUrl());
//				show.setSubCenterUrlScanDate(ser.getShow().getSubCenterUrlScanDate());
//			}
//		}
//		emailService.notifyOfMissingSubtitles(missing);
    }

    @Override
    protected Collection<Subtitles> preDownloadPhase(Set<SubtitlesRequest> requests, boolean forceDownload) {
        Set<Subtitles> cachedSubtitles;
        if (forceDownload) {
            cachedSubtitles = Collections.emptySet();
        } else {
            cachedSubtitles = skipCachedSubtitles(requests);
            skipScannedSubtitles(requests);
        }
        return cachedSubtitles;
    }

    private void skipScannedSubtitles(Set<SubtitlesRequest> requests) {
        Date backlogDate = DateUtils.getPastDate(14);
        for (SubtitlesRequest request : new ArrayList<>(requests)) {
            for (SubtitleLanguage subtitleLanguage : request.getLanguages()) {
                if (request instanceof SubtitlesEpisodeRequest) {
                    SubtitlesEpisodeRequest sser = (SubtitlesEpisodeRequest) request;
                    Date scanDate = null;
                    SubtitlesScanHistory subtitleScanHistory = subtitlesService.findSubtitleScanHistory(request.getTorrent(), subtitleLanguage);
                    if (subtitleScanHistory != null) {
                        scanDate = subtitleScanHistory.getScanDate();
                    }
                    if (scanDate != null && sser.getAirDate() != null && sser.getAirDate().before(backlogDate)) {
                        request.getLanguages().remove(subtitleLanguage);
                        logService.info(getClass(), "Skipping downloading " + subtitleLanguage + " subtitles for '" + request.getTorrent() + "' - already scanned and airdate is older than 14 days ago");
                    }
                }
            }

            if (request.getLanguages().isEmpty()) {
                requests.remove(request);
            }
        }
    }

    private Set<Subtitles> skipCachedSubtitles(Set<SubtitlesRequest> requests) {
        Set<Subtitles> result = new HashSet<>();
        for (SubtitlesRequest request : new ArrayList<>(requests)) {
            Subtitles subtitles = findSubtitles(request);
            if (subtitles != null) {
                result.add(subtitles);
                request.getLanguages().remove(subtitles.getLanguage());
                logService.info(getClass(), "Found subtitles in cache: " + subtitles + " for torrent: " + request.getTorrent());
                break;
            }

            if (request.getLanguages().isEmpty()) {
                requests.remove(request);
            }
        }
        return result;
    }

    private Subtitles findSubtitles(SubtitlesRequest request) {
        for (SubtitleLanguage subtitleLanguage : new ArrayList<>(request.getLanguages())) {
            for (Subtitles subtitles : subtitlesService.find(request, subtitleLanguage)) {
                for (CachedShow curShow : showSearchService.statisticMatch(subtitles.getName())) {
                    if (request.getName().equals(curShow.getName())) {
                        return subtitles;
                    }
                }
            }
        }

        return null;
    }

    @Override
    protected boolean isSingleTransaction() {
        return false;
    }

    @Override
    protected boolean validateSearchResult(SubtitlesRequest mediaRequest, SearchResult searchResult) {
        return true;
    }

    @Override
    protected Collection<Subtitles> processSingleSearchResult(SubtitlesRequest subtitlesRequest, SearchResult searchResult) {
        for (Subtitles subtitles : searchResult.<Subtitles>getDownloadables()) {
            Subtitles persistedSubtitles = findSubtitles(subtitlesRequest);
            if (persistedSubtitles == null) {
                persistedSubtitles = subtitlesService.findByName(subtitles.getName());
            }

            if (persistedSubtitles != null) {
                logService.info(getClass(), "Found matching existing subtitles, only connecting: " + subtitles);
                persistedSubtitles.getTorrentIds().addAll(subtitles.getTorrentIds());
            } else {
                logService.info(getClass(), "Persisting new subtitles: " + subtitles);
                subtitlesService.persist(subtitles);
//				subtitlesTrackerService.announce(subtitles);
            }
        }

        updateScanDate(subtitlesRequest);

        updateSubcenterOnShow(subtitlesRequest);

        return searchResult.getDownloadables();
    }

    private void updateSubcenterOnShow(SubtitlesRequest subtitlesRequest) {
        if (subtitlesRequest instanceof SubtitlesEpisodeRequest) {
            SubtitlesEpisodeRequest ser = (SubtitlesEpisodeRequest) subtitlesRequest;
            // update subCenter url of the show, in the request episode it must not be null, otherwise couldn't find the results
            Show show = showService.find(ser.getShow().getId());
            show.setSubCenterUrl(ser.getShow().getSubCenterUrl());
            show.setSubCenterUrlScanDate(ser.getShow().getSubCenterUrlScanDate());
        }
    }

    @Override
    protected List<Subtitles> processSearchResults(Collection<Pair<SubtitlesRequest, SearchResult>> results) {
        throw new UnsupportedOperationException();
//		List<Subtitles> res = new ArrayList<>();
//		for (Pair<SubtitlesRequest, SearchResult> result : results) {
//			SubtitlesRequest subtitlesRequest = result.getKey();
//			SearchResult searchResult = result.getValue();
//
//
//			res.addAll(searchResult.<Subtitles>getDownloadables());
//		}
//		return res;
    }

    private void updateScanDate(SubtitlesRequest subtitlesRequest) {
        for (SubtitleLanguage subtitleLanguage : subtitlesRequest.getLanguages()) {
            SubtitlesScanHistory subtitleScanHistory = subtitlesService.findSubtitleScanHistory(subtitlesRequest.getTorrent(), subtitleLanguage);
            if (subtitleScanHistory == null) {
                subtitleScanHistory = subtitlesService.factory().createSubtitlesScanHistory();
                subtitleScanHistory.setLanguage(subtitleLanguage);
                subtitleScanHistory.setTorrent(subtitlesRequest.getTorrent());
            }
            subtitleScanHistory.setScanDate(new Date());
        }
    }

    @Override
    protected SearchResult downloadTorrent(SubtitlesRequest request) {
        return subCenterSubtitlesSearcher.search(request);
    }
}
