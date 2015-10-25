package rss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rss.cache.UserCacheService;
import rss.entities.News;
import rss.permissions.PermissionsService;
import rss.services.NewsService;
import rss.user.User;
import rss.user.UserService;
import rss.user.context.UserContextHolder;
import rss.util.JsonTranslation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 10/02/13 18:16
 */
@Path("/admin")
@Component
public class AdminResource {

    @Autowired
    private PermissionsService permissionsService;

    @Autowired
    private NewsService newsService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserCacheService userCacheService;

    @RequestMapping(value = "/notification", method = RequestMethod.POST)
    @ResponseBody
    public Response sendNotification(@QueryParam("text") String text) {
        permissionsService.verifyAdminPermissions();

        userService.sendEmailToAllUsers(text);

        return Response.ok().build();
    }

    @Path("/news")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response createNews(@QueryParam("text") String text) {
        permissionsService.verifyAdminPermissions();

        News news = new News();
        news.setMessage(text);
        newsService.createNews(news);

        Map<String, Object> result = new HashMap<>();
        result.put("id", news.getId());
        return Response.ok().entity(JsonTranslation.object2JsonString(result)).build();
    }

    @Path("/news/dismiss")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response dismissNews() {
        permissionsService.verifyAdminPermissions();
        User user = userCacheService.getUser(UserContextHolder.getCurrentUserContext().getUserId());
        newsService.dismissNews(user);
        return Response.ok().build();
    }
}