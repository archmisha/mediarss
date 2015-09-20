package rss.dao;

import rss.ems.dao.Dao;
import rss.entities.SearchLog;
import rss.services.searchers.SearchResult;
import rss.torrents.requests.MediaRequest;

/**
 * User: dikmanm
 * Date: 10/02/2015 13:42
 */
public interface SearchLogDao extends Dao<SearchLog> {
    void logSearch(MediaRequest mediaRequest, String name, SearchResult.SearchStatus searchStatus, String url);

    void logSearch(MediaRequest mediaRequest, String name, SearchResult.SearchStatus searchStatus, String url, String page, String exception);
}
