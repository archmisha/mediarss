package rss.scheduler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 24/02/2015 09:53
 */
@Service
class SchedulerLifecycleManager {

    private static final Logger LOGGER = LogManager.getLogger(SchedulerLifecycleManager.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private SchedulerService schedulerService;

    @PostConstruct
    public void postConstruct() {
        try {
            this.loadCronTriggerFactoryBeans(applicationContext);
        } catch (Exception e) {
            LOGGER.error("Failed starting quartz scheduler: " + e.getMessage(), e);
        }
    }

    @PreDestroy
    public void preDestroy() {
        try {
            LOGGER.info("Shutting down quartz scheduler");
            scheduler.shutdown();
        } catch (SchedulerException e) {
            LOGGER.info(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadCronTriggerFactoryBeans(ApplicationContext applicationContext) {
        // using parent because we are here with the dispatcher context - it wont find anything on itself
        Map<String, ScheduledJob> quartzJobBeans = applicationContext.getBeansOfType(ScheduledJob.class);
        for (ScheduledJob job : quartzJobBeans.values()) {
            try {
                String jobName = job.getName();
                JobDetail jobDetail = JobBuilder.newJob(ScheduledJobRunner.class)
                        .withIdentity(jobName)
                        .usingJobData("jobName", jobName)
                        .build();
                Trigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(jobName + "_trigger")
                        .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExp())).build();

                scheduler.scheduleJob(jobDetail, trigger);
                schedulerService.createJob(jobName);
                LOGGER.info("Loading scheduled job: " + jobName);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
