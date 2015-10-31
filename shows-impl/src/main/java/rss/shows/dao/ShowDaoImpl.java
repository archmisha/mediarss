package rss.shows.dao;

import org.springframework.stereotype.Repository;
import rss.ems.dao.BaseDaoJPA;
import rss.shows.CachedShow;
import rss.torrents.Show;
import rss.user.User;

import java.util.Collections;
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
    protected Class<? extends Show> getPersistentClass() {
        return ShowImpl.class;
    }

    @Override
    public Show findByName(String name) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("name", name.toLowerCase());
        return uniqueResult(super.<Show>findByNamedQueryAndNamedParams("Show.findByName", params));
    }

	/*@Override
    public Collection<Show> findNotEnded() {
		Map<String, Object> params = new HashMap<>(0);
		return super.findByNamedQueryAndNamedParams("Show.getNotEnded", params);
	}*/

	/*@Override
	public List<Show> autoCompleteShowNames(String term) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("term", "%" + term.toLowerCase() + "%");
		return super.findByNamedQueryAndNamedParams("Show.autoCompleteShowNames", params);
	}*/

    @Override
    public List<CachedShow> findCachedShows() {
        return super.findByNamedQueryAndNamedParams("Show.findCachedShows", null);
    }

    @Override
    public Show findByTvRageId(int tvRageId) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("tvRageId", tvRageId);
        return uniqueResult(super.<Show>findByNamedQueryAndNamedParams("Show.findByTvRageId", params));
    }

    @Override
    public Show findByTheTvDbId(long theTvDbId) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("theTvDbId", theTvDbId);
        return uniqueResult(super.<Show>findByNamedQueryAndNamedParams("Show.findByTheTvDbId", params));
    }

    @Override
    public boolean isShowBeingTracked(Show show) {
        return getUsersCountTrackingShow(show) > 0;
    }

    @Override
    public long getUsersCountTrackingShow(Show show) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("showId", show.getId());
        return uniqueResult(super.<Long>findByNamedQueryAndNamedParams("Show.getUsersCountTrackingShow", params));
    }

    @Override
    public List<Show> getUserShows(User user) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("userId", user.getId());
        return super.findByNamedQueryAndNamedParams("Show.getUserShows", params);
    }

    @Override
    public List<Show> getShowsWithoutTheTvDbId() {
        return super.findByNamedQueryAndNamedParams("Show.getShowsWithoutTheTvDbId", Collections.<String, Object>emptyMap());
    }
}
