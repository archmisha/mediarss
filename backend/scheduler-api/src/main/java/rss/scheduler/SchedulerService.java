package rss.scheduler;

import java.util.List;

/**
 * User: dikmanm
 * Date: 26/02/2015 23:24
 */
public interface SchedulerService {

    void createJob(String jobName);

    JobStatusJson executeJob(String name);

    JobStatusJson getJobStatus(String name);

    List<JobStatusJson> getAllJobs();
}
