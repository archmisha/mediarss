package rss.dao;

import rss.ems.dao.Dao;
import rss.entities.User;
import rss.entities.View;

/**
 * User: dikmanm
 * Date: 14/05/13 18:28
 */
public interface ViewDao extends Dao<View> {

	View find(User user, long objectId);
}
