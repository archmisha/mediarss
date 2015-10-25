package rss.test.services;

import org.springframework.stereotype.Component;
import rss.scheduler.JobStatusJson;
import rss.test.util.JsonTranslation;
import rss.test.util.WaitUtil;

import static junit.framework.Assert.assertTrue;

/**
 * User: dikmanm
 * Date: 12/02/2015 22:43
 */
@Component
public class AdminService extends BaseService {

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
