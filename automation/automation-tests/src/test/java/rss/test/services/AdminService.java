package rss.test.services;

import org.springframework.stereotype.Component;
import rss.scheduler.JobStatusJson;
import rss.test.entities.NewsCreateResult;
import rss.test.util.JsonTranslation;
import rss.test.util.WaitUtil;

import java.util.Collections;

import static junit.framework.Assert.assertTrue;

/**
 * User: dikmanm
 * Date: 12/02/2015 22:43
 */
@Component
public class AdminService extends BaseService {

    public long createNews(String message) {
        reporter.info("Creating news with message '" + message + "'");
        String response = sendPostRequest("rest/admin/news", Collections.<String, Object>singletonMap("text", message));
        return JsonTranslation.jsonString2Object(response, NewsCreateResult.class).getId();
    }

    public void dismissNews() {
        reporter.info("Dismissing news");
        sendGetRequest("rest/admin/news/dismiss");
    }

    public void runDownloadShowListJob() {
        reporter.info("Start download shows list job");
        sendGetRequest("rest/jobs/start/" + "ShowsListDownloader");

        reporter.info("Wait for job to finish");
        WaitUtil.waitFor(new Runnable() {
            @Override
            public void run() {
                String response = sendGetRequest("rest/jobs/" + "ShowsListDownloader");
                JobStatusJson jobStatus = JsonTranslation.jsonString2Object(response, JobStatusJson.class);
                assertTrue(jobStatus.getEnd() != null && jobStatus.getEnd() > jobStatus.getStart());
            }
        });
    }
}
