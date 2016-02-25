package rss.news;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.cache.UserCacheService;
import rss.news.dao.NewsImpl;
import rss.permissions.PermissionsService;
import rss.user.User;
import rss.user.context.UserContextHolder;
import rss.util.JsonTranslation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dikmanm on 26/10/2015.
 */
@Path("/news")
@Component
public class NewsResource {

    @Autowired
    private NewsService newsService;

    @Autowired
    private PermissionsService permissionsService;

    @Autowired
    private UserCacheService userCacheService;

    @Path("/")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(propagation = Propagation.REQUIRED)
    public Response createNews(@QueryParam("text") String text) {
        permissionsService.verifyAdminPermissions();

        News news = new NewsImpl();
        news.setMessage(text);
        newsService.createNews(news);

        Map<String, Object> result = new HashMap<>();
        result.put("id", news.getId());
        return Response.ok().entity(JsonTranslation.object2JsonString(result)).build();
    }

    @Path("/dismiss")
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
