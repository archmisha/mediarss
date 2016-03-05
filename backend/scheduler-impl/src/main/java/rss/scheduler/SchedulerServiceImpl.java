package rss.scheduler;

import com.mongodb.client.model.Filters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import rss.environment.Environment;
import rss.environment.ServerMode;
import rss.log.LogService;
import rss.mail.EmailService;
import rss.rms.ResourceManagementService;
import rss.rms.query.RmsQueryInformation;
import rss.util.DurationMeter;
import rss.util.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * User: dikmanm
 * Date: 10/02/13 18:50
 */
@Service
public class SchedulerServiceImpl implements SchedulerService {

    @Autowired
    protected LogService logService;
    @Autowired
    private ResourceManagementService rmsService;
    @Autowired
    private EmailService emailService;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void createJob(String jobName) {
        JobStatusJson jobStatus = getJobStatus(jobName);
        if (jobStatus == null) {
            jobStatus = new JobStatusJson();
            jobStatus.setName(jobName);
            rmsService.saveOrUpdate(jobStatus, JobStatusJson.class);
        }
    }

    @Override
    public JobStatusJson executeJob(final String name) {
        final ScheduledJob job = getJob(name);
        JobStatusJson jobStatus = getJobStatus(name);
        if (jobStatus == null) {
            jobStatus = new JobStatusJson();
            jobStatus.setName(job.getName());
        }

        if (isJobRunning(jobStatus)) {
            return jobStatus;
        }

        logService.info(getClass(), "Job " + name + " started");
        final DurationMeter durationMeter = new DurationMeter();
        updateJobStarted(jobStatus, durationMeter.getStartTime());

        final Class<?> aClass = getClass();
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                String statusMessage = null;
                try {
                    job.run();
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    if (Utils.isRootCauseMessageContains(e, "timed out")) {
                        logService.error(aClass, e.getMessage());
                        sw.append(e.getMessage());
                        statusMessage = "Failed";
                    } else {
                        logService.error(aClass, e.getMessage(), e);
                        e.printStackTrace(new PrintWriter(sw));
                        statusMessage = e.getMessage();
                    }

                    emailService.notifyOfFailedJob("Job " + name + " fail at " + durationMeter.getStartTime() + ":\r\n\r\n" + sw);
                }

                durationMeter.stop();
                Date end = durationMeter.getEndTime();
                updateJobFinished(name, end, statusMessage);

                logService.info(aClass, String.format("Job " + name + " completed. Time took %d ms.", durationMeter.getDuration()));
            }
        });

        return jobStatus;
    }

    @Override
    public JobStatusJson getJobStatus(final String name) {
        RmsQueryInformation queryInformation = rmsService.factory().createRmsQueryBuilder()
                .filter(Filters.eq("name", name)).getRmsQueryInformation();
        return rmsService.get(rmsService.factory().createGetResourceOperation(JobStatusJson.class, queryInformation));
    }

    @Override
    public List<JobStatusJson> getAllJobs() {
        RmsQueryInformation queryInformation = rmsService.factory().createRmsQueryBuilder().getRmsQueryInformation();
        List<JobStatusJson> jobs = rmsService.getCollection(rmsService.factory().createGetResourceOperation(JobStatusJson.class, queryInformation));

        // if a job started before the server was up, then was a problem with a job and should mark it as stopped
        for (JobStatusJson job : jobs) {
            if (job.getEnd() == null && job.getStart() != null && new Date(job.getStart()).before(Environment.getInstance().getStartupDate())) {
                job.setEnd(Environment.getInstance().getStartupDate().getTime());
            }
        }

        // so that jobs appear in the same order in the ui every time
        Collections.sort(jobs, new Comparator<JobStatusJson>() {
            @Override
            public int compare(JobStatusJson o1, JobStatusJson o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return jobs;
    }

    private boolean isJobRunning(JobStatusJson jobStatus) {
        if (jobStatus.getEnd() == null && jobStatus.getStart() != null) {
            // in test mode job can run only for 10 seconds, otherwise something went wrong
            if (Environment.getInstance().getServerMode() == ServerMode.TEST) {
                return jobStatus.getStart() < TimeUnit.SECONDS.toMillis(10);
            }
            return true;
        }
        return false;
    }

    private ScheduledJob getJob(String name) {
        Map<String, ScheduledJob> jobs = new HashMap<>();
        for (ScheduledJob job : applicationContext.getBeansOfType(ScheduledJob.class).values()) {
            jobs.put(job.getName(), job);
        }

        if (!jobs.containsKey(name)) {
            throw new InvalidParameterException("Unknown job name: " + name);
        }

        return jobs.get(name);
    }

    private void updateJobFinished(final String name, final Date end, final String statusMessage) {
        JobStatusJson jobStatus = getJobStatus(name);
        jobStatus.setEnd(end.getTime());
        jobStatus.setErrorMessage(statusMessage);
        rmsService.saveOrUpdate(jobStatus, JobStatusJson.class);
    }

    private void updateJobStarted(final JobStatusJson jobStatus, final Date startTime) {
        jobStatus.setStart(startTime.getTime());
        jobStatus.setEnd(null);
        jobStatus.setErrorMessage(null);
        rmsService.saveOrUpdate(jobStatus, JobStatusJson.class);
    }
}