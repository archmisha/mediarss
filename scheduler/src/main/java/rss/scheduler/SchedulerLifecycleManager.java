package rss.scheduler;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 24/02/2015 09:53
 */
@Service
public class SchedulerLifecycleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerLifecycleManager.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Scheduler scheduler;

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
        Map<String, Object> quartzJobBeans = applicationContext.getBeansWithAnnotation(QuartzJob.class);
        for (Map.Entry<String, Object> entry : quartzJobBeans.entrySet()) {
            try {
                Object job = entry.getValue();
                QuartzJob quartzJobAnnotation = applicationContext.findAnnotationOnBean(entry.getKey(), QuartzJob.class);
                if (!Job.class.isAssignableFrom(job.getClass())) {
                    throw new RuntimeException(job.getClass() + " doesn't implemented " + Job.class);
                }

//                CronTriggerBean trigger = new CronTriggerBean();
//                trigger.setCronExpression(quartzJobAnnotation.cronExp());
//                trigger.setName(quartzJobAnnotation.name() + "_trigger");
//                JobDetail jobDetail = new JobDetailBean();
//                jobDetail.setName(quartzJobAnnotation.name());
//                jobDetail.setJobClass(job.getClass());
//                trigger.setJobDetail(jobDetail);

                JobDetail jobDetail = JobBuilder.newJob((Class<? extends Job>) job.getClass()).withIdentity(quartzJobAnnotation.name()).build();
                Trigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(quartzJobAnnotation.name() + "_trigger")
                        .withSchedule(CronScheduleBuilder.cronSchedule(quartzJobAnnotation.cronExp())).build();

                scheduler.scheduleJob(jobDetail, trigger);
                LOGGER.info("Loading quartz Job: " + entry.getValue());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
