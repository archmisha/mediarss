package rss.test.entities;

/**
 * User: dikmanm
 * Date: 15/08/2015 10:41
 */
public class InvalidParametersResponse {
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
