package rss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import rss.EmailAlreadyRegisteredException;
import rss.RegisterException;
import rss.dao.UserDao;
import rss.entities.User;
import rss.services.EmailService;
import rss.services.SessionService;
import rss.services.SettingsService;
import rss.services.UserServiceImpl;
import rss.services.log.LogService;
import rss.services.subtitles.SubtitleLanguage;
import rss.util.DurationMeter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController extends BaseController {

	@Autowired
	private UserDao userDao;

	@Autowired
	private EmailService emailService;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private LogService logService;

	@RequestMapping(value = "/pre-login/{tab}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> getPreLoginData(@PathVariable String tab) {
		Map<String, Object> result = new HashMap<>();
		result.put("isLoggedIn", false);
		if (sessionService.isUserLogged()) {
			User user = userDao.find(sessionService.getLoggedInUserId());
			result = createTabData(user);
			result.put("isLoggedIn", true);
		}

		return result;
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> login(@RequestParam("username") String email,
									 @RequestParam("password") String password,
									 @RequestParam("tab") String tab) {
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
	public void logout(HttpServletResponse response) {
		sessionService.clearLoggedInUser();
		try {
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
		result.put("userSubtitles", user.getSubtitles());
		result.put("tvShowsRssFeed", userService.getTvShowsRssFeed(user));
		result.put("moviesRssFeed", userService.getMoviesRssFeed(user));
		duration.stop();
		logService.info(getClass(), "initialData " + duration.getDuration() + " millis");

		return result;
	}

	private Map<String, Object> createTabData(User user) {
		Map<String, Object> result = new HashMap<>();
		result.put("isAdmin", isAdmin(user));
		result.put("deploymentDate", settingsService.getDeploymentDate());
		result.put("firstName", user.getFirstName());
		return result;
	}
}