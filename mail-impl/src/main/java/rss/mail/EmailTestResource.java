package rss.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rss.environment.Environment;
import rss.environment.ServerMode;
import rss.util.JsonTranslation;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 07/03/2015 11:59
 */
@Path("/mail/test")
@Component
public class EmailTestResource {

    @Autowired
    private TestEmailProvider testEmailEngine;

    @Path("/get")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getEmail(String json) {
        // verify test mode
        if (Environment.getInstance().getServerMode() != ServerMode.TEST) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final Map jsonMap = JsonTranslation.jsonString2Object(json, Map.class);
        String email = String.valueOf(jsonMap.get("email"));
        Collection<EmailJSON> emails = testEmailEngine.getByEmail(email);

        return Response.ok().entity(JsonTranslation.object2JsonString(emails.toArray(new EmailJSON[emails.size()]))).build();
    }
}
