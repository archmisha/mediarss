package rss.user.context;

import rss.environment.Environment;

import java.util.Set;

/**
 * User: dikmanm
 * Date: 20/02/2015 14:42
 */
public class UserContextImpl implements UserContext {

    private long userId;
    private String email;
    private boolean isAdmin;

    public UserContextImpl(long userId, String email, boolean isAdmin) {
        this.userId = userId;
        this.email = email;
        this.isAdmin = isAdmin;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public boolean isAdmin() {
        if (isAdmin) {
            return true;
        }

        Set<String> adminEmails = Environment.getInstance().getAdministratorEmails();
        if (adminEmails.contains(email)) {
            return true;
        }

        return false;
    }
}