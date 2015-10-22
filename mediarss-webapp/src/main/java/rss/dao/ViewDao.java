package rss.dao;

import rss.ems.dao.Dao;
import rss.entities.View;
import rss.user.User;

/**
 * User: dikmanm
 * Date: 14/05/13 18:28
 */
public interface ViewDao extends Dao<View> {

	View find(User user, long objectId);
}
