package rss.user;

/**
 * Created by michaeld on 06/03/2016.
 */
public class UserRegisterResult {
    private User user;
    private String status;

    public UserRegisterResult(User user, String status) {
        this.user = user;
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public String getStatus() {
        return status;
    }
}
