package rss.test.tests;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import rss.test.entities.UserData;
import rss.test.entities.UserLoginResult;
import rss.test.services.TraktService;
import rss.test.services.UserService;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * User: dikmanm
 * Date: 12/02/2015 22:44
 */
public class TraktTest extends BaseTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TraktService traktService;

    @Test
    public void testConnectAndDisconnectToTrakt() {
        reporter.info("Create admin user and login");
        UserData adminUser = UserData.createAdminUser("user1", "user1");
        userService.createUser(adminUser);
        UserLoginResult userLoginResult = userService.login(adminUser);
        assertFalse(userLoginResult.isConnectedToTrakt());

        reporter.info("Simulate trakt redirect to webapp after authentication and verify connected to trakt");
        traktService.redirectAfterAuth();
        userLoginResult = userService.preLogin(adminUser);
        assertTrue(userLoginResult.isConnectedToTrakt());

        reporter.info("Disconnect from trakt");
        traktService.disconnect();
        userLoginResult = userService.preLogin(adminUser);
        assertFalse(userLoginResult.isConnectedToTrakt());
    }
}
