package rss.test.services;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 23/02/2015 18:17
 */
@Component
public class TraktService extends BaseService {

    public void redirectAfterAuth() {
        reporter.info("Simulating trakt redirect to app after auth");
        Map<String, String> params = new HashMap<>();
        params.put("code", "test_code");
        params.put("state", "test_state");
        sendGetRequest("/rest/trakt/auth", params);
    }

    public void disconnect() {
        reporter.info("Simulating trakt redirect to app after auth");
        sendGetRequest("/rest/trakt/disconnect");
    }
}
