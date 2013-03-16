package rss.controllers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import rss.MediaRSSException;
import rss.NoPermissionsException;
import rss.UserNotLoggedInException;
import rss.controllers.vo.ShowVO;
import rss.controllers.vo.UserResponse;
import rss.dao.JobStatusDao;
import rss.dao.UserTorrentDao;
import rss.entities.JobStatus;
import rss.entities.Torrent;
import rss.entities.User;
import rss.entities.UserTorrent;
import rss.services.SettingsService;
import rss.services.UserService;
import rss.services.log.LogService;
import rss.services.movies.MovieService;
import rss.services.movies.MoviesScrabblerImpl;

import javax.servlet.http.HttpServletRequest;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * User: dikmanm
 * Date: 07/01/13 20:01
 */
@SuppressWarnings("UnusedDeclaration")
public class BaseController {

	public static final String TVSHOWS_TAB = "tvshows";
	public static final String MOVIES_TAB = "movies";
	public static final String ADMIN_TAB = "admin";

	@Autowired
	private LogService log;

	@Autowired
	protected UserTorrentDao userTorrentDao;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private MovieService movieService;

	@Autowired
	private JobStatusDao jobStatusDao;

	@Autowired
	protected EntityConverter entityConverter;

	@Autowired
	protected UserService userService;

	protected LogService getLogService() {
		return log;
	}

	protected String extractString(HttpServletRequest request, String name, boolean isMandatory) {
		String value = request.getParameter(name);
		if (isMandatory) {
			if (StringUtils.isBlank(value)) {
				throw new InvalidParameterException(name + " can not be empty");
			}
//		} else if (value == null) {
//			throw new InvalidParameterException(name + " is not given");
		}

		return value;
	}

	protected int extractMandatoryInteger(HttpServletRequest request, String name) {
		try {
			return Integer.parseInt(extractString(request, name, true));
		} catch (NumberFormatException e) {
			throw new InvalidParameterException(name + " is not an integer");
		}
	}

	protected int extractInteger(HttpServletRequest request, String name, int defaultValue) {
		try {
			return Integer.parseInt(extractString(request, name, false));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	protected boolean extractBoolean(HttpServletRequest request, String name, boolean isMandatory) {
		return Boolean.parseBoolean(extractString(request, name, isMandatory));
	}

	@ExceptionHandler(InvalidParameterException.class)
	@ResponseBody
	public ExceptionResponse handleInvalidParameterException(InvalidParameterException ex) {
		return new ExceptionResponse().withMessage(ex.getMessage());
	}

	@ExceptionHandler(UserNotLoggedInException.class)
	@ResponseBody
	public ExceptionResponse handleUserNotLoggedInException(UserNotLoggedInException ex) {
		return new ExceptionResponse().withMessage("User is not logged in");
	}

	@ExceptionHandler(MediaRSSException.class)
	@ResponseBody
	public ExceptionResponse handleMediaRSSException(MediaRSSException ex) {
		if (ex.log()) {
			log.error(getClass(), ex.getMessage(), ex);
		}
		return new ExceptionResponse().withMessage(ex.getUserMessage());
	}

	@ExceptionHandler(NoPermissionsException.class)
	@ResponseBody
	public ExceptionResponse handleNoPermissionsException(NoPermissionsException ex) {
		return new ExceptionResponse().withMessage("Access denied");
	}

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ExceptionResponse handleException(Exception ex) {
		log.error(getClass(), ex.getMessage(), ex);
		return new ExceptionResponse().withMessage("Oops. We've got some error.");
	}

	protected void verifyAdminPermissions(User user) {
		if (!settingsService.getAdministratorEmails().contains(user.getEmail())) {
			String msg = "Detected impersonation of admin user. User: " + user.getEmail();
			getLogService().error(getClass(), msg);
			throw new NoPermissionsException(msg);
		}
	}

	// note: getters, setters and empty ctor are needed for spring to json serialization
	protected static class ExceptionResponse {
		private String message;
		private boolean success;

		public ExceptionResponse() {
			success = false;
		}

		public ExceptionResponse withMessage(String message) {
			this.message = message;
			return this;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public boolean isSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}
	}

	protected UserTorrent addUserTorrent(User user, Torrent torrent, UserTorrent userTorrent) {
		userTorrent.setUser(user);
		userTorrent.setAdded(new Date());
		userTorrent.setTorrent(torrent);
		userTorrentDao.persist(userTorrent);
		return userTorrent;
	}

	protected UserResponse createUserResponse(User user, String tab) {
		UserResponse userResponse = userService.getUserResponse(user);

		if (tab.equals(MOVIES_TAB)) {
			userResponse.withMoviesLastUpdated(getMoviesLastUpdated())
					.withMovies(movieService.getUserMovies(user))
					.withFutureMovies(movieService.getFutureUserMovies(user));
		} else if (tab.equals(TVSHOWS_TAB)) {
			userResponse.withShows(sort(entityConverter.toThinShows(user.getShows())));
		}

		return userResponse;
	}

	private Date getMoviesLastUpdated() {
		JobStatus jobStatus = jobStatusDao.find(MoviesScrabblerImpl.JOB_NAME);
		if (jobStatus == null) {
			return null;
		}
		return jobStatus.getStart();
	}

	private List<ShowVO> sort(List<ShowVO> shows) {
		Collections.sort(shows, new Comparator<ShowVO>() {
			@Override
			public int compare(ShowVO o1, ShowVO o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		return shows;
	}
}
