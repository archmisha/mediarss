package rss.torrents.searchers.config;

import java.util.Collection;

/**
 * User: dikmanm
 * Date: 07/01/14 18:49
 */
public interface SearcherConfigurationDao {

	SearcherConfiguration findByName(String name);

	Collection<SearcherConfiguration> getAll();

	void persist(SearcherConfiguration searcherConfiguration);

	SearcherConfiguration merge(SearcherConfiguration searcherConfiguration);
}
