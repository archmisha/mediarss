package rss.services.shows;

import rss.entities.Show;

import java.util.Collection;

/**
 * User: dikmanm
 * Date: 15/03/13 10:31
 */
public interface ShowsCacheService {

	void put(Show show);

	Collection<CachedShow> getAll();

	Collection<String> getShowNamePermutations(CachedShow show);
}
