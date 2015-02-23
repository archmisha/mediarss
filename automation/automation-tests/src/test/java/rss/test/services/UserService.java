package rss.test.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import rss.test.entities.UserData;
import rss.test.entities.UserLoginResult;
import rss.test.entities.UserRegisterResult;
import rss.test.util.JsonTranslation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * User: dikmanm
 * Date: 12/02/2015 22:43
 */
@Component
public class UserService extends BaseService {

    public void createUser(UserData user) {
        reporter.info("Creating user '" + user.getUsername() + "'");
        user.setUsername(user.getUsername() + "_" + UUID.randomUUID().toString());
        if (StringUtils.isBlank(user.getFirstName())) {
            user.setFirstName("firstName");
        }
        if (StringUtils.isBlank(user.getLastName())) {
            user.setLastName("lastName");
        }
        String response = sendPostRequest("rest/user/register", entityToMap(user));
        UserRegisterResult userRegisterResult = JsonTranslation.jsonString2Object(response, UserRegisterResult.class);
        assertTrue(userRegisterResult.isSuccess());
    }

    public UserLoginResult login(UserData user) {
        reporter.info("Login with user '" + user.getUsername() + "'");
        Map<String, Object> params = new HashMap<>();
        params.put("username", user.getUsername());
        params.put("password", user.getPassword());
        String response = sendPostRequest("rest/user/login", params);
        return JsonTranslation.jsonString2Object(response, UserLoginResult.class);
    }

    public UserLoginResult preLogin(UserData user) {
        reporter.info("Call pre-login with user '" + user.getUsername() + "'");
        String response = sendGetRequest("rest/user/pre-login");
        return JsonTranslation.jsonString2Object(response, UserLoginResult.class);
    }
}
