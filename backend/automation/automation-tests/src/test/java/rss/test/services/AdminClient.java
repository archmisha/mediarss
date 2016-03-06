package rss.test.services;

import org.springframework.stereotype.Component;
import rss.scheduler.JobStatusJson;
import rss.test.util.json.JsonTranslation;
import rss.test.util.WaitUtil;

import static junit.framework.Assert.assertTrue;

/**
 * User: dikmanm
 * Date: 12/02/2015 22:43
 */
@Component
public class AdminClient extends BaseClient {

    @Override
    protected String getServiceName() {
        return "jobs";
    }

    public void runDownloadShowsScheduleJob() {
        triggerJob("ShowsScheduleDownloaderJob");
    }

    private void triggerJob(final String jobName) {
        reporter.info("Start download shows list job");
        httpUtils.sendGetRequest("rest/jobs/start/" + jobName);

        reporter.info("Wait for job to finish");
        WaitUtil.waitFor(new Runnable() {
            @Override
            public void run() {
                String response = httpUtils.sendGetRequest(getBasePath() + "/" + jobName);
                JobStatusJson jobStatus = JsonTranslation.jsonString2Object(response, JobStatusJson.class);
                assertTrue(jobStatus.getEnd() != null && jobStatus.getEnd() > jobStatus.getStart());
            }
        });
    }
}
