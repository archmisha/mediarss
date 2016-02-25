package rss.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

/**
 * User: dikmanm
 * Date: 26/02/2015 23:22
 */
@Component
class ScheduledJobRunner extends QuartzJobBean {

    public static final String JOB_NAME_PARAMETER = "jobName";

    @Autowired
    private SchedulerService schedulerService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String jobName = jobExecutionContext.getMergedJobDataMap().getString(JOB_NAME_PARAMETER);
        schedulerService.executeJob(jobName);
    }
}