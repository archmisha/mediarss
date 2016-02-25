package rss.test.entities;

/**
 * User: dikmanm
 * Date: 13/02/2015 12:14
 */
public class UserRegisterResult {
    private boolean success;
    private String message;
    private long userId;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
