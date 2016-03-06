package rss.test.services;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 23/02/2015 18:17
 */
@Component
public class TraktClient extends BaseClient {

    @Override
    protected String getServiceName() {
        return "trakt";
    }

    public void redirectAfterAuth() {
        reporter.info("Simulating trakt redirect to app after auth");
        Map<String, String> params = new HashMap<>();
        params.put("code", "test_code");
        params.put("state", "test_state");
        httpUtils.sendGetRequest(getBasePath() + "/auth", params);
    }

    public void disconnect() {
        reporter.info("Simulating trakt redirect to app after auth");
        httpUtils.sendGetRequest(getBasePath() + "/disconnect");
    }
}
