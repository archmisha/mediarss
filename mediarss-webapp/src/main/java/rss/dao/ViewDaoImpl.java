package rss.dao;

import org.springframework.stereotype.Repository;
import rss.ems.dao.BaseDaoJPA;
import rss.entities.View;
import rss.user.User;

import java.util.HashMap;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 14/05/13 18:28
 */
@Repository
public class ViewDaoImpl extends BaseDaoJPA<View> implements ViewDao {

	@Override
	public View find(User user, long objectId) {
		Map<String, Object> params = new HashMap<>(2);
		params.put("userId", user.getId());
		params.put("objectId", objectId);
		return uniqueResult(super.<View>findByNamedQueryAndNamedParams("View.findByObjectId", params));
	}
}
