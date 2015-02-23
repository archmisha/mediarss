package rss.controllers;

import com.google.common.base.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import rss.MediaRSSException;
import rss.UserNotLoggedInException;
import rss.configuration.SettingsService;
import rss.log.LogService;
import rss.permissions.NoPermissionsException;
import rss.services.SessionService;
import rss.services.shows.ShowAutoCompleteItem;
import rss.services.shows.ShowService;
import rss.services.user.UserCacheService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.InvalidParameterException;
import java.util.*;

/**
 * User: dikmanm
 * Date: 07/01/13 20:01
 */
@SuppressWarnings("UnusedDeclaration")
public class BaseController {

	public static final String HOME_TAB = "home";
	public static final String TVSHOWS_TAB = "tvshows";
	public static final String MOVIES_TAB = "movies";
	public static final String ADMIN_TAB = "admin";

	@Autowired
	protected LogService logService;

	@Autowired
	protected UserCacheService userCacheService;

	@Autowired
	protected SettingsService settingsService;

	@Autowired
	protected EntityConverter entityConverter;

	@Autowired
	protected ShowService showService;

	@Autowired
	protected SessionService sessionService;

	protected <T> T applyDefaultValue(T value, T defaultValue) {
		return value == null ? defaultValue : value;
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
			logService.error(getClass(), ex.getMessage(), ex);
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
		logService.error(getClass(), ex.getMessage(), ex);
		return new ExceptionResponse().withMessage("Oops. We've got some error.");
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

	protected Map<String, Object> autoCompleteShowNames(HttpServletRequest request, HttpServletResponse response, boolean includeEnded,
														Predicate<? super ShowAutoCompleteItem> predicate) {
//		try {
		String term = extractString(request, "term", true).trim();
//			String callback = extractString(request, "callback", true);

		List<ShowAutoCompleteItem> result = showService.autoCompleteShowNames(term, includeEnded, predicate);
		Collections.sort(result, new Comparator<ShowAutoCompleteItem>() {
			@Override
			public int compare(ShowAutoCompleteItem o1, ShowAutoCompleteItem o2) {
				return o1.getText().compareToIgnoreCase(o2.getText());
			}
		});

		response.setContentType("text/javascript");

		Map<String, Object> map = new HashMap<>();
		map.put("shows", result);
		map.put("total", result.size());
		return map;
//			Gson gson = new Gson();
//			String data = gson.toJson(map);
//			IOUtils.write(callback + "(" + data + ")", response.getOutputStream());
//		} catch (IOException e) {
//			throw new MediaRSSException(e.getMessage(), e);
//		}
	}
}
