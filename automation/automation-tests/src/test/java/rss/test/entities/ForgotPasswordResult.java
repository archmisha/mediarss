package rss.test.entities;

/**
 * User: dikmanm
 * Date: 07/03/2015 11:38
 */
public class ForgotPasswordResult {

    private String message;
    private boolean success;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
