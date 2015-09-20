package rss.scheduler;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.JobDetailBean;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerLifecycleManager.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private SchedulerService schedulerService;

    @PostConstruct
    public void postConstruct() {
        try {
            this.loadCronTriggerBeans(applicationContext);
        } catch (Exception e) {
            LOGGER.error("Failed starting quartz scheduler: " + e.getMessage(), e);
        }
    }

    @PreDestroy
    public void preDestroy() {
        try {
            LOGGER.info("Shutting down quartz scheduler");
//            ServletContext servletContext = servletContextEvent.getServletContext();
//            WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
//            Scheduler scheduler = springContext.getBean(Scheduler.class);
            scheduler.shutdown();
        } catch (SchedulerException e) {
            LOGGER.info(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadCronTriggerBeans(ApplicationContext applicationContext) {
        // using parent because we are here with the dispatcher context - it wont find anything on itself
        Map<String, ScheduledJob> quartzJobBeans = applicationContext.getBeansOfType(ScheduledJob.class);
        for (ScheduledJob job : quartzJobBeans.values()) {
            try {
                String jobName = job.getName();

                CronTriggerBean trigger = new CronTriggerBean();
                trigger.setCronExpression(job.getCronExp());
                trigger.setName(jobName + "_trigger");
                JobDetail jobDetail = new JobDetailBean();
                jobDetail.setName(jobName);
                jobDetail.setJobClass(ScheduledJobRunner.class);
                jobDetail.getJobDataMap().put(ScheduledJobRunner.JOB_NAME_PARAMETER, jobName);
                trigger.setJobDetail(jobDetail);

//                JobDetail jobDetail = JobBuilder.newJob(ScheduledJobRunner.class)
//                        .withIdentity(jobName)
//                        .usingJobData("jobName", jobName)
//                        .build();
//                Trigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(jobName + "_trigger")
//                        .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExp())).build();

                scheduler.scheduleJob(jobDetail, trigger);
                schedulerService.createJob(jobName);
                LOGGER.info("Loading scheduled job: " + jobName);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
