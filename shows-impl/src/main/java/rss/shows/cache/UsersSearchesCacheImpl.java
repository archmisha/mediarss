package rss.shows.cache;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import rss.services.shows.UserActiveSearch;

import java.util.ArrayList;
import java.util.List;

/**
 * User: dikmanm
 * Date: 06/11/13 23:27
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class UsersSearchesCacheImpl implements UsersSearchesCache {

    private List<UserActiveSearch> searches = new ArrayList<>();

    public void addSearch(UserActiveSearch search) {
        this.searches.add(search);
    }

    public List<UserActiveSearch> getSearches() {
        return searches;
    }

    public void removeSearch(String id) {
        for (UserActiveSearch userActiveSearch : new ArrayList<>(searches)) {
            if (userActiveSearch.getSearchResultJSON().getId().equals(id)) {
                searches.remove(userActiveSearch);
            }
        }
    }
}
