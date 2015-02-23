package rss.permissions;

/**
 * User: dikmanm
 * Date: 21/02/2015 12:39
 */
public interface PermissionsService {

    boolean isAdmin();

    void verifyAdminPermissions();
}
