package rss.services.shows;

import rss.entities.Show;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
