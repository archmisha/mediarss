package rss.dao;

import rss.entities.SearchLog;
import rss.services.requests.MediaRequest;
import rss.services.searchers.SearchResult;

/**
 * User: dikmanm
 * Date: 10/02/2015 13:42
 */
public interface SearchLogDao extends Dao<SearchLog> {
    void logSearch(MediaRequest mediaRequest, String name, SearchResult.SearchStatus searchStatus, String url);

    void logSearch(MediaRequest mediaRequest, String name, SearchResult.SearchStatus searchStatus, String url, String page, String exception);
}
