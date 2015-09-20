package rss.services.shows;

import rss.entities.Episode;
import rss.services.downloader.DownloadResult;
import rss.services.requests.episodes.ShowRequest;
import rss.shows.SearchResultJSON;

/**
 * User: dikmanm
 * Date: 06/11/13 23:32
 */
public class UserActiveSearch {

    private SearchResultJSON searchResultJSON;
    private DownloadResult<Episode, ShowRequest> downloadResult;

    public UserActiveSearch(SearchResultJSON searchResultJSON, DownloadResult<Episode, ShowRequest> downloadResult) {
        this.searchResultJSON = searchResultJSON;
        this.downloadResult = downloadResult;
    }

    public SearchResultJSON getSearchResultJSON() {
        return searchResultJSON;
    }

    public DownloadResult<Episode, ShowRequest> getDownloadResult() {
        return downloadResult;
    }
}
