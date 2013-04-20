package rss.services;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import rss.UserNotLoggedInException;
import rss.entities.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Michael Dikman
 * Date: 01/12/12
 * Time: 13:50
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class SessionServiceImpl implements SessionService {

	// not holding the actual user, cuz then need to make him be in sync with the database all the time
	private Long loggedInUserId;
	private Date prevLoginDate;

	public void setLoggedInUser(User user) {
		this.loggedInUserId = user.getId();
		prevLoginDate = user.getLastLogin();
		if (prevLoginDate == null) {
			prevLoginDate = new Date();
		}
	}

	@Override
	public Long getLoggedInUserId() {
		if (loggedInUserId == null) {
			throw new UserNotLoggedInException();
		}
		return loggedInUserId;
	}

	@Override
	public boolean isUserLogged() {
		return loggedInUserId != null;
	}

	@Override
	public void clearLoggedInUser() {
		loggedInUserId = null;
		prevLoginDate = null;
	}

	@Override
	public Date getPrevLoginDate() {
		return prevLoginDate;
	}
}
