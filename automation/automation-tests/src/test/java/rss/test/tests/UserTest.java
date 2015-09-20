package rss.test.tests;

import com.google.common.base.Function;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import rss.test.entities.UserData;
import rss.test.entities.UserLoginResult;
import rss.test.entities.UserResult;
import rss.test.services.EmailService;
import rss.test.services.NoPermissionsException;
import rss.test.services.RedirectToRootException;
import rss.test.util.AssertUtils;

import java.security.InvalidParameterException;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * User: dikmanm
 * Date: 12/02/2015 22:44
 */
public class UserTest extends BaseTest {

    @Autowired
    private EmailService emailService;

    @Test
    public void testLoginPreLoginLogout() {
        UserLoginResult userLoginResult;

        reporter.info("Create admin user");
        UserData adminUser = userService.createAdminUser();

        reporter.info("Call rest without login and verify getting code 403 and redirect to root");
        try {
            userService.preLogin(adminUser);
            fail();
        } catch (RedirectToRootException e) {
        }

        reporter.info("Login and call rest successfully");
        userLoginResult = userService.login(adminUser);
        verifyUserLoginResult(adminUser, userLoginResult);
        userLoginResult = userService.preLogin(adminUser);
        verifyUserLoginResult(adminUser, userLoginResult);

        reporter.info("Logout, call rest and verify getting code 403 and redirect to root");
        userService.logout();
        try {
            userService.preLogin(adminUser);
            fail();
        } catch (RedirectToRootException e) {
        }
    }

    @Test
    public void testLoginNonExistingUser() {
        try {
            reporter.info("Login with non existing username");
            UserData dummyUser = new UserData();
            dummyUser.setUsername("a");
            dummyUser.setPassword("a");
            userService.login(dummyUser);
            fail();
        } catch (InvalidParameterException e) {
            assertEquals("Username or password are incorrect", e.getMessage());
        }
    }

    @Test
    public void testLoginWrongPassword() {
        try {
            reporter.info("Login with wrong password");
            UserData user = userService.createUser();
            user.setPassword("a");
            userService.login(user);
            fail();
        } catch (InvalidParameterException e) {
            assertEquals("Username or password are incorrect", e.getMessage());
        }
    }

    @Test
    public void testLoginWithNotValidatedUser() {
        UserData userData = new UserData();
        userData.setValidated(false);
        UserData regUser = userService.createUser(userData);
        try {
            userService.login(regUser);
        } catch (InvalidParameterException e) {
            assertEquals("Account email is not validated. Please validate before logging in", e.getMessage());
        }
        assertEquals(1, emailService.getEmailsSentTo(regUser.getUsername()).size());
    }

    @Test(expected = NoPermissionsException.class)
    public void testGetAllUsersPermissions() {
        reporter.info("Create regular user and login");
        UserData regUser = userService.createUser();
        userService.login(regUser);

        reporter.info("Verify call to get all users fail");
        userService.getAllUsers();
    }

    @Test(expected = NoPermissionsException.class)
    public void testImpersonatePermissions() {
        reporter.info("Create 2 regular users and one admin");
        UserData regUser = userService.createUser();
        UserData adminUser = userService.createAdminUser();
        UserData regUser2 = userService.createUser();

        reporter.info("Login as regular user");
        userService.login(regUser);
        UserResult regUser2Result = userService.getUser(regUser2);

        reporter.info("Verify call to impersonate him fail");
        userService.impersonate(regUser2Result);
    }

    @Test
    public void testImpersonateGetAllUsers() {
        UserLoginResult userLoginResult;

        reporter.info("Create admin user");
        UserData adminUser = userService.createAdminUser();

        reporter.info("Create regular user");
        UserData regUser = userService.createUser();

        reporter.info("Login as regular user");
        userService.login(regUser);

        reporter.info("Login as admin user");
        userService.login(adminUser);
        userLoginResult = userService.preLogin(adminUser);
        verifyUserLoginResult(adminUser, userLoginResult);

        reporter.info("Get all users and verify sorted by last login");
        List<UserResult> users = userService.getAllUsers();
        assertTrue(AssertUtils.contains(users, userResultToEmailFunc(), adminUser.getUsername(), regUser.getUsername()));

        reporter.info("Impersonate regular user");
        UserResult regUserResult = userService.getUser(regUser);
        userService.impersonate(regUserResult);
        userLoginResult = userService.preLogin(adminUser); // sending adminUser but actually should be regular user
        userLoginResult.setAdmin(false); // returns true, override for comparison because under impersonation
        verifyUserLoginResult(regUser, userLoginResult);

        reporter.info("Impersonate back admin user");
        UserResult adminUserResult = userService.getUser(adminUser);
        userService.impersonate(adminUserResult);
        userLoginResult = userService.preLogin(adminUser);
        verifyUserLoginResult(adminUser, userLoginResult);

        reporter.info("Verify logout still logouts");
        userService.logout();
        try {
            userService.preLogin(adminUser);
            fail();
        } catch (RedirectToRootException e) {
        }
    }

    @Test
    public void testForgotPasswordNotExistingUser() {
        try {
            reporter.info("Call forgot password with non existing username");
            UserData dummyUser = new UserData();
            dummyUser.setUsername("a");
            dummyUser.setPassword("a");
            userService.forgotPassword(dummyUser);
            fail();
        } catch (InvalidParameterException e) {
            assertEquals("Email does not exist", e.getMessage());
        }
    }

    @Test
    public void testForgotPasswordNotValidatedUser() {
        reporter.info("Create user");
        UserData userData = new UserData();
        userData.setValidated(false);
        UserData regUser = userService.createUser(userData);

        String message = userService.forgotPassword(regUser);
        assertEquals("Account validation link was sent to your email address", message.trim());
        assertEquals(1, emailService.getEmailsSentTo(regUser.getUsername()).size());
    }

    @Test
    public void testForgotPasswordValidatedUser() {
        reporter.info("Create user");
        UserData userData = new UserData();
        userData.setValidated(false);
        UserData regUser = userService.createUser(userData);

        reporter.info("Get user hash with admin user");
        UserData adminUser = userService.createAdminUser();
        userService.login(adminUser);
        UserResult regUserResult = userService.getUser(regUser);
        userService.logout();

        reporter.info("Validate user with wrong hash");
        String hash = regUserResult.getValidationHash();
        regUserResult.setValidationHash("wrongHash");
        String result = userService.validateUser(regUserResult);
        assertEquals("Invalid url. Please contact support for assistance", result.trim());

        reporter.info("Validate user with correct hash");
        regUserResult.setValidationHash(hash);
        userService.validateUser(regUserResult);

        String message = userService.forgotPassword(regUser);
        assertEquals("Password recovery email was sent to your email account", message.trim());
        assertEquals(1, emailService.getEmailsSentTo(regUser.getUsername()).size());
    }

    private Function<UserResult, String> userResultToEmailFunc() {
        return new Function<UserResult, String>() {
            @Override
            public String apply(UserResult userResult) {
                return userResult.getEmail();
            }
        };
    }

    private void verifyUserLoginResult(UserData userData, UserLoginResult userLoginResult) {
        assertEquals(userData.getFirstName(), userLoginResult.getFirstName());
        assertNotNull(userLoginResult.getMoviesRssFeed());
        assertNotNull(userLoginResult.getTvShowsRssFeed());
        assertNotNull(userLoginResult.getDeploymentDate());
        assertEquals(userData.isAdmin(), userLoginResult.isAdmin());
        assertEquals(false, userLoginResult.isConnectedToTrakt());
    }
}
