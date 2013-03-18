package rss;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Log4jConfigurer;
import rss.services.OOTBContentLoader;
import rss.services.SettingsService;
import rss.util.QuartzJob;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class AppStartupListener implements ApplicationListener<ContextRefreshedEvent> {

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private OOTBContentLoader ootbContentLoader;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// only if it is the dispatcher web application context - proceed. if root the stop. otherwise happen twice
		ApplicationContext springContext = event.getApplicationContext();
		if (!springContext.getId().contains("dispatcher")) {
			return;
		}

		String base = ".";
		if (System.getProperty("catalina.base") != null) {
			base = System.getProperty("catalina.base");
		} else if (System.getProperty("jetty.home") != null) {
			base = System.getProperty("jetty.home");
		}
		String logsFolder = base + File.separator + "logs";
		System.setProperty("rssFeed.logs_folder", logsFolder);
		System.out.println("setting up logs folder to: " + logsFolder);

		try {
			// no need for /WEB-INF/classes/ prefix
			File log4jPropsFile = new ClassPathResource("log4j.properties", AppStartupListener.class.getClassLoader()).getFile();
			String path = log4jPropsFile.getAbsolutePath();
			Log4jConfigurer.initLogging(path, 30);
			LogFactory.getLog(AppStartupListener.class).info("Log4j system initialized from " + path);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		// debug
		LogFactory.getLog(AppStartupListener.class).info("springContext.getId()=" + springContext.getId());

		AutowireCapableBeanFactory autowireCapableBeanFactory = springContext.getAutowireCapableBeanFactory();
		autowireCapableBeanFactory.autowireBean(this);

		try {
			ootbContentLoader.loadTVRageShows();
		} catch (Exception e) {
			LogFactory.getLog(AppStartupListener.class).error("Failed loading OOTB content: " + e.getMessage(), e);
		}

		try {
			this.loadCronTriggerBeans(springContext);
		} catch (Exception e) {
			LogFactory.getLog(AppStartupListener.class).error("Failed starting quartz scheduler: " + e.getMessage(), e);
		}

		// track deployment date
		Date deployedDate = getDeploymentDate();
		settingsService.setDeploymentDate(deployedDate);

		settingsService.setStartupDate(new Date());
	}

	@SuppressWarnings("unchecked")
	private void loadCronTriggerBeans(ApplicationContext applicationContext) {
		Log log = LogFactory.getLog(AppStartupListener.class);

		Map<String, Object> quartzJobBeans = applicationContext.getBeansWithAnnotation(QuartzJob.class);
		for (Map.Entry<String, Object> entry : quartzJobBeans.entrySet()) {
			try {
				Object job = entry.getValue();
				QuartzJob quartzJobAnnotation = applicationContext.findAnnotationOnBean(entry.getKey(), QuartzJob.class);
				if (!Job.class.isAssignableFrom(job.getClass())) {
					throw new RuntimeException(job.getClass() + " doesn't implemented " + Job.class);
				}

				JobDetail jobDetail = JobBuilder.newJob((Class<? extends Job>) job.getClass()).withIdentity(quartzJobAnnotation.name()).build();
				Trigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(quartzJobAnnotation.name() + "_trigger")
						.withSchedule(CronScheduleBuilder.cronSchedule(quartzJobAnnotation.cronExp())).build();

				scheduler.scheduleJob(jobDetail, trigger);
				log.info("Loading quartz Job: " + entry.getValue());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private Date getDeploymentDate() {
		Log log = LogFactory.getLog(AppStartupListener.class);
		Date deployedDate = new Date(); // better than null, even if wrong
		try {
			String databaseProperties = System.getProperty("database.properties");
			if (databaseProperties == null) {
				databaseProperties = "database.properties";
			}

			// use existing file to locate the real path
			ClassPathResource refClassPathResource = new ClassPathResource(databaseProperties, AppStartupListener.class.getClassLoader());
			String path = refClassPathResource.getURI().getPath();
			path = path.substring(0, path.indexOf(databaseProperties));
			path = path + "deploymentDate.txt";
			File file = new File(path);
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				deployedDate = DATE_FORMAT.parse(br.readLine());
			} else {
				deployedDate = new Date();
//				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file, false);
				fos.write(DATE_FORMAT.format(deployedDate).getBytes());
				fos.close();
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
		return deployedDate;
	}
}
