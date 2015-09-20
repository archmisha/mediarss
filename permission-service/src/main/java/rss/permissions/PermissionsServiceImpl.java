package rss.permissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.context.UserContextHolder;
import rss.log.LogService;

/**
 * User: dikmanm
 * Date: 21/02/2015 12:36
 */
@Service
public class PermissionsServiceImpl implements PermissionsService {

    @Autowired
    private LogService logService;

    @Override
    public boolean isAdmin() {
        // if logged in user is admin
        if (UserContextHolder.getCurrentUserContext().isAdmin()) {
            return true;
        }

        // if impersonated
        if (UserContextHolder.getActualUserContext().isAdmin()) {
            return true;
        }
        return false;
    }

    @Override
    public void verifyAdminPermissions() {
        if (!isAdmin()) {
            String msg = "User " + UserContextHolder.getCurrentUserContext().getEmail() + " has no admin permissions";
            logService.error(getClass(), msg);
            throw new NoPermissionsException(msg);
        }
    }
}
