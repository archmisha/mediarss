package rss.user.dao;

import rss.ems.dao.Dao;
import rss.user.User;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface UserDao extends Dao<User> {

    User findByEmail(String email);
}