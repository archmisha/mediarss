package rss;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Log4jConfigurer;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import rss.services.OOTBContentLoader;
import rss.services.PageDownloader;
import rss.services.SettingsService;
import rss.services.shows.ShowService;
import rss.services.shows.ShowsScheduleDownloaderService;
import rss.util.QuartzJob;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

/**
 * User: Michael Dikman
 * Date: 11/05/12
 * Time: 13:58
 */
public class AppConfigListener implements ServletContextListener {

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private ShowService showService;

	@Autowired
	private ShowsScheduleDownloaderService showsScheduleDownloaderService;

	@Autowired
	private OOTBContentLoader ootbContentLoader;

	@Autowired
	private PageDownloader pageDownloader;

	private static Log log = LogFactory.getLog(AppConfigListener.class);

	@Override
	public void contextInitialized(ServletContextEvent event) {
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
			File log4jPropsFile = new ClassPathResource("log4j.properties", AppConfigListener.class.getClassLoader()).getFile();
			String path = log4jPropsFile.getAbsolutePath();
			Log4jConfigurer.initLogging(path, 30);
			LogFactory.getLog(AppConfigListener.class).info("Log4j system initialized from " + path);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
		AutowireCapableBeanFactory autowireCapableBeanFactory = springContext.getAutowireCapableBeanFactory();
		autowireCapableBeanFactory.autowireBean(this);

		try {
			ootbContentLoader.loadTVRageShows();
		} catch (Exception e) {
			LogFactory.getLog(AppConfigListener.class).error("Failed loading OOTB content: " + e.getMessage(), e);
		}

		try {
			this.loadCronTriggerBeans(springContext);
		} catch (Exception e) {
			LogFactory.getLog(AppConfigListener.class).error("Failed starting quartz scheduler: " + e.getMessage(), e);
		}

		// track deployment date
		Date deployedDate = getDeploymentDate();
		settingsService.setDeploymentDate(deployedDate);
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		Log log = LogFactory.getLog(AppConfigListener.class);

		try {
			log.info("Shutting down quartz scheduler");
			ServletContext servletContext = servletContextEvent.getServletContext();
			WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
			Scheduler scheduler = springContext.getBean(Scheduler.class);
			scheduler.shutdown();
		} catch (SchedulerException e) {
			log.info(e.getMessage(), e);
		}

		// to shutdown the quartz scheduler nicely
		BeanFactory bf = ContextLoader.getCurrentWebApplicationContext();
		if (bf instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext) bf).close();
		}

		// shutdown jdbc drivers
		// This manually uregisters JDBC driver, which prevents Tomcat 7 from complaining about memory leaks wrto this class
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			try {
				DriverManager.deregisterDriver(driver);
				log.info(String.format("De-Registering JDBC driver: %s", driver));
			} catch (SQLException e) {
				log.error(String.format("Error De-Registering driver %s", driver), e);
			}
		}

		try {
			log.info("Sleeping 1000ms to finish shutting down");
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			log.info(e.getMessage(), e);
		}

		Log4jConfigurer.shutdownLogging();

		/*Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for (Thread thread : threadSet) {
			System.out.println("--------------------------");
			for (StackTraceElement stackTraceElement : thread.getStackTrace()) {
				System.out.println(stackTraceElement.toString());
			}
		}*/

	}

	@SuppressWarnings("unchecked")
	private void loadCronTriggerBeans(ApplicationContext applicationContext) {
		Log log = LogFactory.getLog(AppConfigListener.class);

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

				scheduler.addJob(jobDetail, true);
				scheduler.scheduleJob(trigger);
				log.info("Loading quartz Job: " + entry.getValue());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private Date getDeploymentDate() {
		Log log = LogFactory.getLog(AppConfigListener.class);
		Date deployedDate = new Date(); // better than null, even if wrong
		try {
			String databaseProperties = System.getProperty("database.properties");
			if (databaseProperties == null) {
				databaseProperties = "database.properties";
			}

			// use existing file to locate the real path
			ClassPathResource refClassPathResource = new ClassPathResource(databaseProperties, AppConfigListener.class.getClassLoader());
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
