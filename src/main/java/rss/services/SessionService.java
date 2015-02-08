package rss.services;

import rss.entities.User;
import rss.services.shows.UsersSearchesCache;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * User: Michael Dikman
 * Date: 01/12/12
 * Time: 13:49
 */
public interface SessionService {

	void setLoggedInUser(User user, HttpServletResponse response, boolean rememberMe);

	Long getLoggedInUserId();

	boolean isUserLoggedIn();

	void restoreUserDataFromCookie(HttpServletRequest request, HttpServletResponse response);

	void clearLoggedInUser(HttpServletRequest request, HttpServletResponse response);

	public Long getImpersonatedUserId();

	void impersonate(Long userId);

	UsersSearchesCache getUsersSearchesCache();

	Date getPrevLoginDate();
}
