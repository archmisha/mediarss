package mediarss.test.services;

import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * User: dikmanm
 * Date: 12/02/2015 22:43
 */
@Component
public class AdminService extends BaseService {

    public long createNews(String message) {
        reporter.info("Creating news with message '" + message + "'");
        String response = sendPostRequest("rest/admin/news", Collections.<String, Object>singletonMap("text", message));
        return Long.parseLong(response);
    }

    public void dismissNews(long newsId) {
        reporter.info("Dismissing news");
        String result = sendGetRequest("rest/admin/news/dismiss");
    }
}
