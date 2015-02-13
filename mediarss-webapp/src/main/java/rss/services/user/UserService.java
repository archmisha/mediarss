package rss.services.user;

import rss.entities.User;

/**
 * User: Michael Dikman
 * Date: 15/12/12
 * Time: 12:10
 */
public interface UserService {

    String register(String firstName, String lastName, final String email, final String password, boolean isAdmin);

	String getMoviesRssFeed(User user);

	String getTvShowsRssFeed(User user);

	User getUser(long userId);
}
