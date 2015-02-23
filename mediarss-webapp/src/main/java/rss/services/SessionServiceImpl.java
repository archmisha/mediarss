package rss.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import rss.entities.User;
import rss.services.shows.UsersSearchesCache;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * User: Michael Dikman
 * Date: 01/12/12
 * Time: 13:50
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class SessionServiceImpl implements SessionService {

	@Autowired
	private HttpSession session;

	private UsersSearchesCache usersSearchesCache = new UsersSearchesCache(); // if for some reason there is no logged in user ...

	public void setLoggedInUser(User user, HttpServletResponse response, boolean rememberMe) {
        usersSearchesCache = new UsersSearchesCache();
    }

	@Override
	public void clearLoggedInUser(HttpServletRequest request, HttpServletResponse response) {
		usersSearchesCache = new UsersSearchesCache();
		session.invalidate();
	}

	public UsersSearchesCache getUsersSearchesCache() {
		return usersSearchesCache;
	}
}
