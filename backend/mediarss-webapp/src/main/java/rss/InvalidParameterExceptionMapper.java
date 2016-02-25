package rss;

import rss.util.JsonTranslation;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.security.InvalidParameterException;

/**
 * User: dikmanm
 * Date: 06/03/2015 20:31
 */
@Provider
public class InvalidParameterExceptionMapper implements ExceptionMapper<InvalidParameterException> {

    @Override
    public Response toResponse(InvalidParameterException exception) {
        final InvalidParametersResponse ipr = new InvalidParametersResponse(exception.getMessage());
        return Response.ok(JsonTranslation.object2JsonString(ipr)).build();
    }

    private class InvalidParametersResponse {
        private String message;
        private boolean success;

        public InvalidParametersResponse(String message) {
            this.message = message;
            this.success = false;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}
