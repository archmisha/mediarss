package rss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import rss.EmailAlreadyRegisteredException;
import rss.RegisterException;
import rss.SubtitleLanguage;
import rss.dao.ShowDao;
import rss.dao.UserDao;
import rss.entities.User;
import rss.services.EmailService;
import rss.services.SessionService;
import rss.services.SettingsService;
import rss.services.UserServiceImpl;
import rss.services.log.LogService;
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
	private ShowDao showDao;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private LogService logService;

	@RequestMapping(value = "/pre-login/{tab}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> getPreLoginData(@PathVariable String tab) {
		DurationMeter duration = new DurationMeter();
		Map<String, Object> result = new HashMap<>();
		result.put("initialData", createInitialData(tab));

		User user = null;


		if (sessionService.isUserLogged()) {
			user = userDao.find(sessionService.getLoggedInUserId());
			result.put("user", createUserResponse(user, tab));
		}

		duration.stop();
		String msg = "getPreLoginData(" + tab + ")";
		if (user != null) {
			msg += " for " + user.getEmail();
		}
		msg += " (" + duration.getDuration() + " millis)";
		logService.info(getClass(), msg);

		return result;
	}

	private Map<String, Object> createInitialData(String tab) {
		Map<String, Object> initialData = new HashMap<>();
		initialData.put("deploymentDate", settingsService.getDeploymentDate());
		initialData.put("subtitles", SubtitleLanguage.getValues());
		return initialData;
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> login(@RequestParam("username") String email,
									 @RequestParam("password") String password,
									 @RequestParam("tab") String tab,
									 @RequestParam("includeInitialData") boolean includeInitialData) {
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

		Map<String, Object> result = new HashMap<>();
		result.put("user", createUserResponse(user, tab));
		if (includeInitialData) {
			result.put("initialData", createInitialData(tab));
		}
		return result;
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
	public void subtitles(HttpServletRequest request) {
		String subtitles = extractString(request, "subtitles", false);

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
}