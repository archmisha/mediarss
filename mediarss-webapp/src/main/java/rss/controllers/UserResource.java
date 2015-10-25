package rss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.cache.UserCacheService;
import rss.environment.Environment;
import rss.environment.ServerMode;
import rss.log.LogService;
import rss.permissions.PermissionsService;
import rss.services.NewsService;
import rss.trakt.TraktService;
import rss.user.*;
import rss.user.context.CookieUtils;
import rss.user.context.SessionUserContext;
import rss.user.context.UserContextHolder;
import rss.user.context.UserContextImpl;
import rss.user.json.UserJSON;
import rss.user.subtitles.SubtitleLanguage;
import rss.util.JsonTranslation;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.security.InvalidParameterException;
import java.util.*;

@Path("/user")
@Component
public class UserResource {

    @Autowired
    private UserService userService;

    @Autowired
    private PermissionsService permissionsService;

    @Autowired
    private LogService logService;

    @Autowired
    private NewsService newsService;

    @Autowired
    private TraktService traktService;

    @Autowired
    private UserCacheService userCacheService;

    @Path("/pre-login")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPreLoginData() {
        boolean loggedIn = !UserContextHolder.isUserContextEmpty();

        Map<String, Object> result = new HashMap<>();
        if (loggedIn) {
            User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
            result = createTabData(user);
        }
        result.put("isLoggedIn", loggedIn);

        return Response.ok().entity(JsonTranslation.object2JsonString(result)).build();
    }

    @Path("/login")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response login(@FormParam("username") String email,
                          @FormParam("password") String password,
                          @FormParam("rememberMe") boolean rememberMe,
                          @Context HttpServletRequest request) {
        email = email.trim();
        password = password.trim();

        User user = userService.findByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            throw new InvalidParameterException("Username or password are incorrect");
        }

        if (!user.isValidated()) {
            // resend account validation link
            userService.sendAccountValidationLink(user); // todo: should keep counter and resend up to 3 times
            throw new InvalidParameterException("Account email is not validated. Please validate before logging in");
        }

        UserContextHolder.cleanUserContext();
        UserContextHolder.pushUserContext(new UserContextImpl(user.getId(), user.getEmail(), user.isAdmin()));
        new SessionUserContext(request.getSession()).storeInSession();

        user.setLastLogin(new Date());
        userCacheService.invalidateUser(user);

        Response.ResponseBuilder responseBuilder = Response.ok();
        if (rememberMe) {
            responseBuilder.cookie(CookieUtils.createRememberMeCookie(user));
        }
        Map<String, Object> tabData = createTabData(user);
        return responseBuilder.entity(JsonTranslation.object2JsonString(tabData)).build();
    }

    @Path("/logout")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@CookieParam(CookieUtils.REMEMBER_ME_COOKIE_NAME) String rememberMeCookie,
                           @Context HttpServletRequest request) {
        try {
            UserContextHolder.cleanUserContext();

            Response.ResponseBuilder responseBuilder = Response.ok();
            CookieUtils.invalidateRememberMeCookie(rememberMeCookie, responseBuilder);
            request.getSession().invalidate();

            responseBuilder.location(new URI("/"));
            return responseBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Path("/register")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response register(@FormParam("firstName") String firstName,
                             @FormParam("lastName") String lastName,
                             @FormParam("username") String email,
                             @FormParam("password") String password,
                             @FormParam("admin") Boolean isAdmin,
                             @FormParam("validated") Boolean isValidated) {
        // isAdmin is valid only for test mode, otherwise always overriding with false
        if (isAdmin == null || Environment.getInstance().getServerMode() != ServerMode.TEST) {
            isAdmin = false;
        }

        Map<String, Object> result = new HashMap<>();
        try {
            String response = userService.register(firstName, lastName, email, password, isAdmin);
            result.put("success", true);
            result.put("message", response);

            if (Environment.getInstance().getServerMode() == ServerMode.TEST && isValidated) {
                final User createdUser = userService.findByEmail(email);
                createdUser.setValidationHash(null);
                result.put("userId", createdUser.getId());
            }
        } catch (EmailAlreadyRegisteredException e) {
            // no need to write to log file in that case
            result.put("success", false);
            result.put("message", e.getMessage());
        } catch (RegisterException e) {
            logService.warn(getClass(), e.getMessage(), e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return Response.ok().entity(JsonTranslation.object2JsonString(result)).build();
    }

    @Path("/forgot-password")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response forgotPassword(String json) {
        final Map jsonMap = JsonTranslation.jsonString2Object(json, Map.class);
        String email = String.valueOf(jsonMap.get("email"));
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new InvalidParameterException("Email does not exist");
        }

        ForgotPasswordResult forgotPasswordResult = userService.forgotPassword(user);

        Map<String, Object> result = new HashMap<>();
        result.put("message", forgotPasswordResult.getMsg());
        result.put("success", true);
        return Response.ok().entity(JsonTranslation.object2JsonString(result)).build();
    }

    @Path("/users")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {
        permissionsService.verifyAdminPermissions();

        List<UserJSON> users = userListToJson(userService.getAllUsers());
        Collections.sort(users, new Comparator<UserJSON>() {
            @Override
            public int compare(UserJSON o1, UserJSON o2) {
                if (o2.getLastLogin() == null) {
                    return -1;
                } else if (o1.getLastLogin() == null) {
                    return 1;
                }
                return o2.getLastLogin().compareTo(o1.getLastLogin());
            }
        });

        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        return Response.ok().entity(JsonTranslation.object2JsonString(result)).build();
    }

    @Path("/impersonate/{userId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response impersonate(@PathParam("userId") Long userId, @Context HttpServletRequest request) {
        permissionsService.verifyAdminPermissions();

        // remove previous impersonations
        UserContextHolder.popOnBehalfUserContexts();

        // if not impersonating back to myself
        if (UserContextHolder.getActualUserContext().getUserId() != userId) {
            User user = userCacheService.getUser(userId);
            UserContextHolder.pushUserContext(new UserContextImpl(user.getId(), user.getEmail(), user.isAdmin()));
        }
        new SessionUserContext(request.getSession()).storeInSession();

        return Response.ok().build();
    }

    @Path("/subtitles")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response subtitles(@QueryParam("subtitles") String subtitles) {
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        user.setSubtitles(SubtitleLanguage.fromString(subtitles));
        return Response.ok().build();
    }

    private Map<String, Object> createTabData(User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("isAdmin", permissionsService.isAdmin());
        result.put("deploymentDate", Environment.getInstance().getDeploymentDate().getTime());
        result.put("firstName", user.getFirstName());
        result.put("tvShowsRssFeed", userService.getTvShowsRssFeed(user));
        result.put("moviesRssFeed", userService.getMoviesRssFeed(user));
        result.put("news", newsService.getNews(user));
        result.put("traktClientId", traktService.getClientId());
        result.put("isConnectedToTrakt", traktService.isConnected(user.getId()));
        return result;
    }

    private List<UserJSON> userListToJson(Collection<User> users) {
        ArrayList<UserJSON> result = new ArrayList<>();
        for (User user : users) {
            result.add(userToJson(user));
        }
        return result;
    }

    private UserJSON userToJson(User user) {
        UserJSON userJSON = new UserJSON()
                .withId(user.getId())
                .withLoggedIn(UserContextHolder.getCurrentUserContext().getUserId() == user.getId())
                .withEmail(user.getEmail())
                .withFirstName(user.getFirstName())
                .withLastName(user.getLastName())
                .withLastLogin(user.getLastLogin())
                .withLastShowsFeedAccess(user.getLastShowsFeedGenerated())
                .withLastMoviesFeedAccess(user.getLastMoviesFeedGenerated())
                .withAdmin(Environment.getInstance().getAdministratorEmails().contains(user.getEmail()));
        if (user.getSubtitles() == null) {
            userJSON.setSubtitles(null);
        } else {
            userJSON.setSubtitles(user.getSubtitles().toString());
        }
        if (Environment.getInstance().getServerMode() == ServerMode.TEST) {
            userJSON.setValidationHash(user.getValidationHash());
        }
        return userJSON;
    }
}