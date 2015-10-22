package rss.cache;

import rss.shows.CachedShow;
import rss.torrents.Show;

import java.util.Collection;

/**
 * User: dikmanm
 * Date: 15/03/13 10:31
 */
public interface ShowsCacheService {

    void put(Show show);

    Collection<CachedShow> getAll();

    Collection<CachedShowSubsetSet> getShowsSubsets();

    void updateShowEnded(Show show);
}
