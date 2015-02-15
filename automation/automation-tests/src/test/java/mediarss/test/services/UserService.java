package mediarss.test.services;

import mediarss.test.entities.UserData;
import mediarss.test.entities.UserLoginResult;
import mediarss.test.entities.UserRegisterResult;
import mediarss.test.util.JsonTranslation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

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
}
