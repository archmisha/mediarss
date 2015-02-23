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
import rss.context.UserContextHolder;
import rss.context.UserContextImpl;
import rss.dao.SubtitlesDao;
import rss.dao.UserDao;
import rss.entities.Subtitles;
import rss.entities.Torrent;
import rss.entities.User;
import rss.environment.Environment;
import rss.environment.ServerMode;
import rss.log.LogService;
import rss.permissions.PermissionsService;
import rss.services.NewsService;
import rss.services.feed.RssFeedGenerator;
import rss.services.subtitles.SubtitleLanguage;
import rss.services.trakt.TraktService;
import rss.services.user.ForgotPasswordResult;
import rss.services.user.UserService;
import rss.util.CookieUtils;
import rss.util.DurationMeter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.InvalidParameterException;
import java.util.*;

@Controller
@RequestMapping("/user")
public class UserController extends BaseController {

    public static final String SESSION_USER_CONTEXT_ATTR = "UserContext";

    @Autowired
    private UserDao userDao;

    @Autowired
    private PermissionsService permissionsService;

    @Autowired
    private LogService logService;

    @Autowired
    private UserService userService;

    @Autowired
    private SubtitlesDao subtitlesDao;

    @Autowired
    private NewsService newsService;

    @Autowired
    private TraktService traktService;

    @Autowired
    @Qualifier("tVShowsRssFeedGeneratorImpl")
    private RssFeedGenerator tvShowsRssFeedGenerator;

    @RequestMapping(value = "/pre-login", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> getPreLoginData() {
        boolean loggedIn = !UserContextHolder.isUserContextEmpty();

        Map<String, Object> result = new HashMap<>();
        if (loggedIn) {
            User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
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
                                     HttpServletRequest request, HttpServletResponse response) {
        email = email.trim();
        password = password.trim();

        User user = userDao.findByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            throw new InvalidParameterException("Username or password are incorrect");
        }

        if (!user.isValidated() && Environment.getInstance().getServerMode() != ServerMode.TEST) {
            // resend account validation link
            userService.sendAccountValidationLink(user);
            throw new InvalidParameterException("Account email is not validated. Please validate before logging in");
        }

        UserContextImpl userContext = new UserContextImpl(user.getId(), user.getEmail(), user.isAdmin());
        request.getSession().setAttribute(SESSION_USER_CONTEXT_ATTR, userContext);
        UserContextHolder.pushUserContext(userContext);
        if (rememberMe) {
            CookieUtils.createRememberMeCookie(user, response);
        }
        sessionService.setLoggedInUser(user, response, rememberMe);
        user.setLastLogin(new Date());

        userCacheService.invalidateUser(user);

        return createTabData(user);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @ResponseBody
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        UserContextHolder.cleanUserContext();
        CookieUtils.invalidateRememberMeCookie(request, response);

        sessionService.clearLoggedInUser(request, response);
        try {
            response.sendRedirect("/");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> register(HttpServletRequest request) {
        String firstName = extractString(request, "firstName", true);
        String lastName = extractString(request, "lastName", true);
        String email = extractString(request, "username", true);
        String password = extractString(request, "password", true);
        boolean isAdmin = false;
        if (Environment.getInstance().getServerMode() == ServerMode.TEST) {
            isAdmin = Boolean.parseBoolean(extractString(request, "admin", false));
        }

        Map<String, Object> result = new HashMap<>();
        try {
            String response = userService.register(firstName, lastName, email, password, isAdmin);
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
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        user.setSubtitles(SubtitleLanguage.fromString(subtitles));
    }

    @RequestMapping(value = "/trakt/disconnect", method = RequestMethod.GET)
    @ResponseBody
    public void traktDisconnect(HttpServletRequest request, HttpServletResponse response) {
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        traktService.disconnectUser(user);
    }

    @RequestMapping(value = "/forgot-password", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> forgotPassword(HttpServletRequest request) {
        String email = extractString(request, "email", true);
        User user = userDao.findByEmail(email);
        if (user == null) {
            throw new InvalidParameterException("Email does not exist");
        }

        ForgotPasswordResult forgotPasswordResult = userService.forgotPassword(user);

        Map<String, Object> result = new HashMap<>();
        result.put("message", forgotPasswordResult.getMsg());
        result.put("success", true);
        return result;
    }

    @RequestMapping(value = "/initial-data", method = RequestMethod.GET)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> initialData() {
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());

        DurationMeter duration = new DurationMeter();
        Map<String, Object> result = new HashMap<>();
        result.put("subtitles", SubtitleLanguage.getValues());
        result.put("userSubtitles", user.getSubtitles() == null ? null : user.getSubtitles().toString());

        Set<Torrent> torrents = tvShowsRssFeedGenerator.getFeedTorrents(user);
        Collection<Subtitles> subtitles = subtitlesDao.find(torrents, user.getSubtitles());
        result.put("recentSubtitles", entityConverter.toThinSubtitles(subtitles, torrents));

        duration.stop();
        logService.info(getClass(), "initialData " + duration.getDuration() + " ms");

        return result;
    }

    private Map<String, Object> createTabData(User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("isAdmin", permissionsService.isAdmin());
        result.put("deploymentDate", Environment.getInstance().getDeploymentDate());
        result.put("firstName", user.getFirstName());
        result.put("tvShowsRssFeed", userService.getTvShowsRssFeed(user));
        result.put("moviesRssFeed", userService.getMoviesRssFeed(user));
        result.put("news", newsService.getNews(user));
        result.put("traktClientId", traktService.getClientId());
        result.put("isConnectedToTrakt", traktService.isConnected(user));
        return result;
    }
}