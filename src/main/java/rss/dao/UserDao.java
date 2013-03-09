package rss.dao;

import rss.entities.Episode;
import rss.entities.User;

import java.util.Collection;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface UserDao extends Dao<User> {

    public User findByEmail(String email);

	Collection<Episode> getEpisodesToDownload(User user);
}
