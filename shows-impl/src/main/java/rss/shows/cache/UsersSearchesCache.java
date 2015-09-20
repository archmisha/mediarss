package rss.shows.cache;

import rss.services.shows.UserActiveSearch;

import java.util.List;

/**
 * User: dikmanm
 * Date: 06/11/13 23:27
 */
public interface UsersSearchesCache {

    void addSearch(UserActiveSearch search);

    List<UserActiveSearch> getSearches();

    void removeSearch(String id);
}
