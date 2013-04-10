package rss.dao;

import org.springframework.stereotype.Repository;
import rss.entities.Episode;
import rss.entities.Show;
import rss.entities.User;

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
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -14);

		Map<String, Object> params = new HashMap<>(2);
		params.put("userId", user.getId());
		params.put("fromDate", c.getTime());
		return super.findByNamedQueryAndNamedParams("User.getEpisodesToDownload", params);
	}

    @Override
	public boolean isShowBeingTracked(Show show) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("showId", show.getId());
        return !super.<User>findByNamedQueryAndNamedParams("User.findByTrackedShow", params).isEmpty();
    }
}
