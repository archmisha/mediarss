package rss.trakt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rss.user.context.UserContextHolder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * User: dikmanm
 * Date: 26/02/2015 10:38
 */
@Path("/trakt")
@Component
public class TraktController {

    @Autowired
    private TraktService traktService;

    @Path("/disconnect")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response traktDisconnect() {
        traktService.disconnectUser(UserContextHolder.getCurrentUserContext().getUserId());
        return Response.ok().build();
    }
}
