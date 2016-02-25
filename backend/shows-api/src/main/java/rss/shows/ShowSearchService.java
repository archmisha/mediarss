package rss.shows;

import rss.torrents.Episode;
import rss.torrents.downloader.DownloadResult;
import rss.torrents.requests.shows.ShowRequest;

import java.util.Collection;

/**
 * User: dikmanm
 * Date: 16/04/13 17:14
 */
public interface ShowSearchService {

    SearchResultJSON search(ShowRequest episodeRequest, long userId, boolean forceDownload);

    Collection<CachedShow> statisticMatch(String name);

    void downloadResultToSearchResultVO(long userId, DownloadResult<Episode, ShowRequest> downloadResult, SearchResultJSON searchResultJSON);
}
