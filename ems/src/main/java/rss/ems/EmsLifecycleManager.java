package rss.ems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * User: dikmanm
 * Date: 24/02/2015 09:53
 */
@Service
public class EmsLifecycleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmsLifecycleManager.class);

    @PostConstruct
    public void postConstruct() {
    }

    @PreDestroy
    public void preDestroy() {
        try {
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
        } catch (Exception e) {
            LOGGER.info(e.getMessage(), e);
        }
    }
}
