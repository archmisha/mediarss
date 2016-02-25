package rss.torrents.searchers.log;

import rss.ems.dao.Dao;
import rss.torrents.requests.MediaRequest;
import rss.torrents.searchers.SearchResult;

/**
 * User: dikmanm
 * Date: 10/02/2015 13:42
 */
public interface SearchLogDao extends Dao<SearchLog> {
    void logSearch(MediaRequest mediaRequest, String name, SearchResult.SearchStatus searchStatus, String url);

    void logSearch(MediaRequest mediaRequest, String name, SearchResult.SearchStatus searchStatus, String url, String page, String exception);
}
