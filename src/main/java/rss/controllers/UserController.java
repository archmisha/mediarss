package rss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import rss.EmailAlreadyRegisteredException;
import rss.RegisterException;
import rss.dao.SubtitlesDao;
import rss.dao.UserDao;
import rss.entities.Subtitles;
import rss.entities.Torrent;
import rss.entities.User;
import rss.services.EmailService;
import rss.services.SettingsService;
import rss.services.UserService;
import rss.services.UserServiceImpl;
import rss.services.feed.RssFeedGenerator;
import rss.services.log.LogService;
import rss.services.subtitles.SubtitleLanguage;
import rss.util.DurationMeter;
import rss.util.StringUtils2;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.InvalidParameterException;
import java.util.*;

@Controller
@RequestMapping("/user")
public class UserController extends BaseController {

	public static final String REMEMBER_ME_COOKIE_NAME = "media-rss";

	@Autowired
	private UserDao userDao;

	@Autowired
	private EmailService emailService;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private LogService logService;

	@Autowired
	private UserService userService;

	@Autowired
	private HttpSession session;

	@Autowired
	private SubtitlesDao subtitlesDao;

	@Autowired
	@Qualifier("tVShowsRssFeedGeneratorImpl")
	private RssFeedGenerator tvShowsRssFeedGenerator;

	@RequestMapping(value = "/pre-login", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> getPreLoginData(HttpServletRequest request) {
		boolean loggedIn = false;
		if (sessionService.isUserLogged()) {
			loggedIn = true;
		} else {
			Cookie cookie = getLoginCookie(request);
			if (cookie != null) {
				String[] arr = cookie.getValue().split(",");
				String email = arr[0];
				String series = arr[1];
				String token = arr[2];
				User user = userDao.findByEmail(email);
				if (user != null && user.getLoginSeries().equals(series)) {
					if (user.getLoginToken().equals(token)) {
						loggedIn = true;
						sessionService.setLoggedInUser(user);
					} else {
						// theft is assumed
						loggedIn = false;
					}
				}
			}
		}

		Map<String, Object> result = new HashMap<>();
		if (loggedIn) {
			User user = userDao.find(sessionService.getLoggedInUserId());
			result = createTabData(user);
		}
		result.put("isLoggedIn", loggedIn);
		return result;
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> login(@RequestParam("username") String email,
									 @RequestParam("password") String password,
									 @RequestParam(value = "rememberMe", required = false, defaultValue = "false") boolean rememberMe,
									 HttpServletResponse response) {
		email = email.trim();
		password = password.trim();

		User user = userDao.findByEmail(email);
		if (user == null || !user.getPassword().equals(password)) {
			throw new InvalidParameterException("Username or password are incorrect");
		}

		if (!user.isValidated()) {
			// resend account validation link
			emailService.sendAccountValidationLink(user);
			throw new InvalidParameterException("Account email is not validated. Please validate before logging in");
		}

		if (rememberMe) {
			user.setLoginSeries(StringUtils2.generateUniqueHash());
			user.setLoginToken(StringUtils2.generateUniqueHash());
			Cookie cookie = new Cookie(REMEMBER_ME_COOKIE_NAME, user.getEmail() + "," + user.getLoginSeries() + "," + user.getLoginToken());
			cookie.setMaxAge(60 * 60 * 24); // 24 hours
			cookie.setPath("/");
			response.addCookie(cookie);
		}

		sessionService.setLoggedInUser(user);

		// important to be after setting the lastLoginDate. session service saves the previous
		user.setLastLogin(new Date());

		return createTabData(user);
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> register(HttpServletRequest request) {
		String firstName = extractString(request, "firstName", true);
		String lastName = extractString(request, "lastName", true);
		String email = extractString(request, "username", true);
		String password = extractString(request, "password", true);

		Map<String, Object> result = new HashMap<>();
		try {
			String response = userService.register(firstName, lastName, email, password);
			result.put("success", true);
			result.put("message", response);
		} catch (EmailAlreadyRegisteredException e) {
			// no need to write to log file in that case
			result.put("success", false);
			result.put("message", e.getMessage());
		} catch (RegisterException e) {
			logService.warn(getClass(), e.getMessage(), e);
			result.put("success", false);
			result.put("message", e.getMessage());
		}

		return result;
	}

	@RequestMapping(value = "/subtitles", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public void subtitles(@RequestParam("subtitles") String subtitles) {
		User user = userDao.find(sessionService.getLoggedInUserId());
		user.setSubtitles(SubtitleLanguage.fromString(subtitles));
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	@ResponseBody
	public void logout(HttpServletRequest request, HttpServletResponse response) {
		sessionService.clearLoggedInUser();
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

	@RequestMapping(value = "/forgot-password", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> forgotPassword(HttpServletRequest request) {
		String email = extractString(request, "email", true);
		User user = userDao.findByEmail(email);
		if (user == null) {
			throw new InvalidParameterException("Email does not exist");
		}

		Map<String, Object> result = new HashMap<>();
		if (user.isValidated()) {
			emailService.sendPasswordRecoveryEmail(user);
			result.put("message", "Password recovery email was sent to your email account");
		} else {
			emailService.sendAccountValidationLink(user);
			result.put("message", UserServiceImpl.ACCOUNT_VALIDATION_LINK_SENT_MESSAGE);
		}
		result.put("success", true);
		return result;
	}

	@RequestMapping(value = "/initial-data", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> initialData() {
		User user = userDao.find(sessionService.getLoggedInUserId());

		DurationMeter duration = new DurationMeter();
		Map<String, Object> result = new HashMap<>();
		result.put("subtitles", SubtitleLanguage.getValues());
		result.put("userSubtitles", user.getSubtitles() == null ? null : user.getSubtitles().toString());
		result.put("tvShowsRssFeed", userService.getTvShowsRssFeed(user));
		result.put("moviesRssFeed", userService.getMoviesRssFeed(user));

		Set<Torrent> torrents = tvShowsRssFeedGenerator.getFeedTorrents(user);
		Collection<Subtitles> subtitles = subtitlesDao.find(torrents, user.getSubtitles());
		result.put("recentSubtitles", entityConverter.toThinSubtitles(subtitles, torrents));

		duration.stop();
		logService.info(getClass(), "initialData " + duration.getDuration() + " ms");

		return result;
	}

	private Cookie getLoginCookie(HttpServletRequest request) {
		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals(REMEMBER_ME_COOKIE_NAME)) {
				return cookie;
			}
		}
		return null;
	}

	private Map<String, Object> createTabData(User user) {
		Map<String, Object> result = new HashMap<>();
		result.put("isAdmin", isAdmin(user));
		result.put("deploymentDate", settingsService.getDeploymentDate());
		result.put("firstName", user.getFirstName());
		return result;
	}
}