package rss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rss.EmailAlreadyRegisteredException;
import rss.RegisterException;
import rss.SubtitleLanguage;
import rss.controllers.vo.ShowVO;
import rss.controllers.vo.UserResponse;
import rss.dao.*;
import rss.entities.*;
import rss.services.*;
import rss.services.downloader.TVShowsTorrentEntriesDownloader;
import rss.services.log.LogService;
import rss.services.movies.MovieService;
import rss.services.movies.MoviesScrabblerImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.InvalidParameterException;
import java.util.*;

@Controller
@RequestMapping("/user")
public class UserController extends BaseController {

	@Autowired
	private UserService userService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private EmailService emailService;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private JobStatusDao jobStatusDao;

	@Autowired
	private ShowDao showDao;

	@Autowired
	private EntityConverter entityConverter;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private LogService logService;

	@Autowired
	private TVShowsTorrentEntriesDownloader torrentEntriesDownloader;

	@Autowired
	private TorrentDao torrentDao;

	@Autowired
	private UserTorrentDao userTorrentDao;

	@Autowired
	private MovieService movieService;

	@RequestMapping(value = "/pre-login/{tab}", method = RequestMethod.GET)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> getPreLoginData(@PathVariable String tab) {
		Map<String, Object> result = new HashMap<>();
		result.put("initialData", createInitialData());

		if (sessionService.isUserLogged()) {
			User user = userDao.find(sessionService.getLoggedInUserId());
			result.put("user", createUserResponse(user));
		}

		return result;
	}

	private Map<String, Object> createInitialData() {
		Map<String, Object> initialData = new HashMap<>();
		initialData.put("deploymentDate", settingsService.getDeploymentDate());
		initialData.put("subtitles", SubtitleLanguage.getValues());
		initialData.put("shows", sort(entityConverter.toThinShows(showDao.findNotEnded())));
		return initialData;
	}

	private ArrayList<ShowVO> sort(ArrayList<ShowVO> shows) {
		Collections.sort(shows, new Comparator<ShowVO>() {
			@Override
			public int compare(ShowVO o1, ShowVO o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		return shows;
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<String, Object> login(HttpServletRequest request) {
		String email = extractString(request, "username", true);
		String password = extractString(request, "password", true);
		boolean includeInitialData = extractBoolean(request, "includeInitialData", true);
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
		result.put("user", createUserResponse(user));
		if (includeInitialData) {
			result.put("initialData", createInitialData());
		}
		return result;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	private UserResponse createUserResponse(User user) {
		return userService.getUserResponse(user)
				.withMoviesLastUpdated(getMoviesLastUpdated())
				.withMovies(movieService.getUserMovies(user));
	}

	private Date getMoviesLastUpdated() {
		JobStatus jobStatus = jobStatusDao.find(MoviesScrabblerImpl.JOB_NAME);
		if (jobStatus == null) {
			return null;
		}
		return jobStatus.getStart();
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