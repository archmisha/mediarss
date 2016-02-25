package rss.permissions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * User: dikmanm
 * Date: 06/03/2015 20:31
 */
@Provider
public class NoPermissionsExceptionMapper implements ExceptionMapper<NoPermissionsException> {

    @Override
    public Response toResponse(NoPermissionsException exception) {
        return Response.status(Response.Status.FORBIDDEN).build();
    }
}
