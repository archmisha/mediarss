package rss;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Log4jConfigurer;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * User: Michael Dikman
 * Date: 11/05/12
 * Time: 13:58
 */
public class AppConfigListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
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
	}
}
