package rss.services;

import rss.entities.User;
import rss.services.shows.UsersSearchesCache;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: Michael Dikman
 * Date: 01/12/12
 * Time: 13:49
 */
public interface SessionService {

	void setLoggedInUser(User user, HttpServletResponse response, boolean rememberMe);

	void clearLoggedInUser(HttpServletRequest request, HttpServletResponse response);

	UsersSearchesCache getUsersSearchesCache();
}
