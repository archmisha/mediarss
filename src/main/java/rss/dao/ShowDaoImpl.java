package rss.dao;

import org.springframework.stereotype.Repository;
import rss.entities.Show;
import rss.services.shows.CachedShow;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Michael Dikman
 * Date: 24/05/12
 * Time: 11:05
 */
@Repository
public class ShowDaoImpl extends BaseDaoJPA<Show> implements ShowDao {

	@Override
	public Show findByName(String name) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("name", name.toLowerCase());
		return uniqueResult(super.<Show>findByNamedQueryAndNamedParams("Show.findByName", params));
	}

	@Override
	public Collection<Show> findNotEnded() {
		Map<String, Object> params = new HashMap<>(0);
		return super.findByNamedQueryAndNamedParams("Show.getNotEnded", params);
	}

	@Override
	public List<Show> autoCompleteShowNames(String term) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("term", "%" + term.toLowerCase() + "%");
		return super.findByNamedQueryAndNamedParams("Show.autoCompleteShowNames", params);
	}

	@Override
	public List<CachedShow> findCachedShows() {
		Map<String, Object> params = new HashMap<>(0);
		return super.findByNamedQueryAndNamedParams("Show.findCachedShows", params);
	}
}
