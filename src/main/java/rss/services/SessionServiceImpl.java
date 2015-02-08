package rss.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import rss.UserNotLoggedInException;
import rss.dao.UserDao;
import rss.entities.User;
import rss.services.shows.UsersSearchesCache;
import rss.util.StringUtils2;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;

/**
 * User: Michael Dikman
 * Date: 01/12/12
 * Time: 13:50
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class SessionServiceImpl implements SessionService {

	public static final String REMEMBER_ME_COOKIE_NAME = "media-rss";

	@Autowired
	private UserDao userDao;

	@Autowired
	private HttpSession session;

	// not holding the actual user, cuz then need to make him be in sync with the database all the time
	private Long loggedInUserId;
	private Long impersonatedUserId;
	private Date prevLoginDate;
	private UsersSearchesCache usersSearchesCache = new UsersSearchesCache(); // if for some reason there is no logged in user ...

	public void setLoggedInUser(User user, HttpServletResponse response, boolean rememberMe) {
		loggedInUserId = user.getId();
		impersonatedUserId = null;
		prevLoginDate = user.getLastLogin();
		usersSearchesCache = new UsersSearchesCache();
		if (prevLoginDate == null) {
			prevLoginDate = new Date();
		}

		if (rememberMe) {
			createRememberMeCookie(user, response);
		}
	}

	@Override
	public Long getLoggedInUserId() {
		if (loggedInUserId == null) {
			throw new UserNotLoggedInException();
		}
		if (impersonatedUserId != null) {
			return impersonatedUserId;
		}
		return loggedInUserId;
	}

	@Override
	public boolean isUserLoggedIn() {
		return loggedInUserId != null;
	}

	public void restoreUserDataFromCookie(HttpServletRequest request, HttpServletResponse response) {
		if (loggedInUserId != null) {
			return;
		}

		Cookie cookie = getLoginCookie(request);
		if (cookie != null) {
			String[] arr = cookie.getValue().split(",");
			String email = arr[0];
			String series = arr[1];
			String token = arr[2];
			User user = userDao.findByEmail(email);
			if (user != null && user.getLoginSeries().equals(series)) {
				if (user.getLoginToken().equals(token)) {
//						loggedIn = true;
					setLoggedInUser(user, response, false);
				} else {
					// theft is assumed
//						loggedIn = false;
				}
			}
		}
	}

	private void createRememberMeCookie(User user, HttpServletResponse response) {
		user.setLoginSeries(StringUtils2.generateUniqueHash());
		user.setLoginToken(StringUtils2.generateUniqueHash());
		Cookie cookie = new Cookie(REMEMBER_ME_COOKIE_NAME, user.getEmail() + "," + user.getLoginSeries() + "," + user.getLoginToken());
		cookie.setMaxAge(Integer.MAX_VALUE);
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	public Long getImpersonatedUserId() {
		return impersonatedUserId;
	}

	@Override
	public void clearLoggedInUser(HttpServletRequest request, HttpServletResponse response) {
		loggedInUserId = null;
		impersonatedUserId = null;
		prevLoginDate = null;
		usersSearchesCache = new UsersSearchesCache();

		session.invalidate();
		try {
			// invalidate cookie
			Cookie cookie = getLoginCookie(request);
			if (cookie != null) {
				cookie = new Cookie(REMEMBER_ME_COOKIE_NAME, null);
				cookie.setMaxAge(0);
				cookie.setValue(null);
				cookie.setPath("/");
				response.addCookie(cookie);
			}

			response.sendRedirect("/");
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public Date getPrevLoginDate() {
		return prevLoginDate;
	}

	public UsersSearchesCache getUsersSearchesCache() {
		return usersSearchesCache;
	}

	@Override
	public void impersonate(Long userId) {
		impersonatedUserId = userId;
	}

	private Cookie getLoginCookie(HttpServletRequest request) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (cookie.getName().equals(REMEMBER_ME_COOKIE_NAME)) {
					return cookie;
				}
			}
		}
		return null;
	}
}
