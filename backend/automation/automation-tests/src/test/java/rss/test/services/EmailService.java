package rss.test.services;

import org.springframework.stereotype.Component;
import rss.mail.EmailJSON;
import rss.test.util.JsonTranslation;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 07/03/2015 11:46
 */
@Component
public class EmailService extends BaseService {

    public Collection<EmailJSON> getEmailsSentTo(String email) {
        reporter.info("Call test email service to verify email sent");
        Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        String response = sendPostRequest("rest/mail/test/get", params);
        return Arrays.asList(JsonTranslation.jsonString2Object(response, EmailJSON[].class));
    }
}
