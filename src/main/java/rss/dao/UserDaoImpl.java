package rss.dao;

import org.springframework.stereotype.Repository;
import rss.entities.Episode;
import rss.entities.Show;
import rss.entities.User;
import rss.util.DateUtils;

import java.util.*;

/**
 * User: Michael Dikman
 * Date: 24/05/12
 * Time: 11:05
 */
@Repository
public class UserDaoImpl extends BaseDaoJPA<User> implements UserDao {

	@Override
	public User findByEmail(String email) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("email", email.toLowerCase());
		return uniqueResult(super.<User>findByNamedQueryAndNamedParams("User.findByEmail", params));
	}

	@Override
	public Collection<Episode> getEpisodesToDownload(User user) {
		// add to feed everything aired since last feed was generated and a buffer of 14 days backwards
		Map<String, Object> params = new HashMap<>(2);
		params.put("userId", user.getId());
		params.put("fromDate", DateUtils.getPastDate(user.getLastShowsFeedGenerated(), 14));
		return super.findByNamedQueryAndNamedParams("User.getEpisodesToDownload", params);
	}

    @Override
	public boolean isShowBeingTracked(Show show) {
//        Map<String, Object> params = new HashMap<>(1);
//        params.put("showId", show.getId());
        return getUsersCountTrackingShow(show) > 0;//!super.<User>findByNamedQueryAndNamedParams("User.findByTrackedShow", params).isEmpty();
    }

	@Override
	public long getUsersCountTrackingShow(Show show) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("showId", show.getId());
		return uniqueResult(super.<Long>findByNamedQueryAndNamedParams("User.getUsersCountTrackingShow", params));
	}
}
