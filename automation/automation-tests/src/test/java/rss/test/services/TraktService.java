package rss.test.services;

import org.springframework.stereotype.Component;

/**
 * User: dikmanm
 * Date: 23/02/2015 18:17
 */
@Component
public class TraktService extends BaseService {

    public void redirectAfterAuth() {
        reporter.info("Simulating trakt redirect to app after auth");
        sendGetRequest("/main?code=test_code&state=test_state");
    }

    public void disconnect() {
        reporter.info("Simulating trakt redirect to app after auth");
        sendGetRequest("/rest/trakt/disconnect");
    }
}
