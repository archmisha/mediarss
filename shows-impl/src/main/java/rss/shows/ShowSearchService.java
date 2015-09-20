package rss.services.shows;

import rss.entities.Episode;
import rss.services.downloader.DownloadResult;
import rss.services.requests.episodes.ShowRequest;
import rss.shows.SearchResultJSON;
import rss.shows.cache.CachedShow;

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
