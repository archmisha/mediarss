package rss.deployer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Log4jConfigurer;
import rss.environment.Environment;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * User: dikmanm
 * Date: 21/02/2015 21:23
 */
public class Deployer {

    private static Logger LOGGER = LoggerFactory.getLogger(Deployer.class);

    public static void main(String[] args) {
        LOGGER.info("Executing lifecycle drivers");
        for (LifecycleDriver lifecycleDriver : Arrays.asList(new H2LifecycleDriver(), new MongoLifecycleDriver())) {
            LOGGER.info(lifecycleDriver.getClass().getSimpleName() + " - tearDown()");
            lifecycleDriver.tearDown();
            LOGGER.info(lifecycleDriver.getClass().getSimpleName() + " - create()");
            lifecycleDriver.create();
        }

        terminate();
        LOGGER.info("Finished executing lifecycle drivers");
    }

    // copied form AppConfigListener
    private static void terminate() {
        // shutdown jdbc drivers
        // This manually unregisters JDBC driver, which prevents Tomcat 7 from complaining about memory leaks wrto this class
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                LOGGER.info(String.format("De-Registering JDBC driver: %s", driver));
            } catch (SQLException e) {
                LOGGER.error(String.format("Error De-Registering driver %s", driver), e);
            }
        }

        Environment.getInstance().shutdown();

        try {
            LOGGER.info("Sleeping 1000ms to finish shutting down");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOGGER.info(e.getMessage(), e);
        }

        Log4jConfigurer.shutdownLogging();
    }
}
