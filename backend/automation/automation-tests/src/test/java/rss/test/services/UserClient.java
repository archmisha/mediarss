package rss.test.services;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rss.test.entities.*;
import rss.test.util.Unique;
import rss.test.util.json.JsonTranslation;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * User: dikmanm
 * Date: 12/02/2015 22:43
 */
@Component
public class UserClient extends BaseClient {

    @Autowired
    private Unique unique;

    @Override
    protected String getServiceName() {
        return "user";
    }

    public UserData createUser() {
        UserData userData = new UserData();
        userData.setUsername(unique.unique());
        userData.setPassword("some password");
        return createAndValidateUser(userData);
    }

    public UserData createAdminUser() {
        UserData userData = new UserData();
        userData.setUsername(unique.unique());
        userData.setPassword("some password");
        userData.setAdmin(true);
        return createAndValidateUser(userData);
    }

    public UserData createAndValidateUser(UserData userData) {
        userData.setValidated(true);
        return createUser(userData);
    }

    public UserData createUser(UserData userData) {
        userData.setUsername(unique.appendUnique(userData.getUsername()));
        reporter.info("Creating user '" + userData.getUsername() + "'");
        if (StringUtils.isBlank(userData.getPassword())) {
            userData.setPassword("some password");
        }
        if (StringUtils.isBlank(userData.getFirstName())) {
            userData.setFirstName("firstName");
        }
        if (StringUtils.isBlank(userData.getLastName())) {
            userData.setLastName("lastName");
        }
        String response = httpUtils.sendFormPostRequest(getBasePath() + "/register", entityToMap(userData));
        UserRegisterResult userRegisterResult = JsonTranslation.jsonString2Object(response, UserRegisterResult.class);
        assertTrue(userRegisterResult.isSuccess());
        userData.setId(userRegisterResult.getUserId());
        return userData;
    }

    public UserLoginResult login(UserData user) {
        reporter.info("Login with user '" + user.getUsername() + "'");
        Map<String, Object> params = new HashMap<>();
        params.put("username", user.getUsername());
        params.put("password", user.getPassword());
        String response = httpUtils.sendFormPostRequest(getBasePath() + "/login", params);
        if (response.contains("\"success\":false")) {
            throw new InvalidParameterException(JsonTranslation.jsonString2Object(response, InvalidParametersResponse.class).getMessage());
        } else {
            return JsonTranslation.jsonString2Object(response, UserLoginResult.class);
        }
    }

    public UserLoginResult preLogin(UserData user) {
        reporter.info("Call pre-login with user '" + user.getUsername() + "'");
        String response = httpUtils.sendGetRequest(getBasePath() + "/pre-login");
        return JsonTranslation.jsonString2Object(response, UserLoginResult.class);
    }

    public void logout() {
        reporter.info("Call logout");
        try {
            httpUtils.sendGetRequest(getBasePath() + "/logout");
        } catch (RedirectToRootException e) {
        }
    }

    public void impersonate(UserResult userResult) {
        reporter.info("Call impersonate with user '" + userResult.getEmail() + "'");
        httpUtils.sendGetRequest(getBasePath() + "/impersonate/" + userResult.getId());
    }

    public List<UserResult> getAllUsers() {
        reporter.info("Call get all users");
        String response = httpUtils.sendGetRequest(getBasePath() + "/users");
        return Arrays.asList(JsonTranslation.jsonString2Object(response, UsersResult.class).getUsers());
    }

    public UserResult getUser(UserData userData) {
        List<UserResult> allUsers = getAllUsers();
        return findUserByEmail(allUsers, userData);
    }

    public String forgotPassword(UserData user) {
        reporter.info("Call forgot password");
        Map<String, Object> params = new HashMap<>();
        params.put("email", user.getUsername());
        String response = httpUtils.sendPostRequest(getBasePath() + "/forgot-password", params);
        if (response.contains("\"success\":false")) {
            throw new InvalidParameterException(JsonTranslation.jsonString2Object(response, InvalidParametersResponse.class).getMessage());
        } else {
            return JsonTranslation.jsonString2Object(response, ForgotPasswordResult.class).getMessage();
        }
    }

    public String validateUser(UserResult userResult) {
        reporter.info("Call register to validate user '" + userResult.getEmail() + "'");
        try {
            return httpUtils.sendGetRequest("register/?user=" + userResult.getId() + "&hash=" + userResult.getValidationHash());
        } catch (RedirectToRootException e) {
            return "";
        }
    }

    private UserResult findUserByEmail(List<UserResult> users, final UserData adminUser) {
        return Collections2.filter(users, new Predicate<UserResult>() {
            @Override
            public boolean apply(UserResult input) {
                return input.getEmail().equals(adminUser.getUsername());
            }
        }).iterator().next();
    }
}
