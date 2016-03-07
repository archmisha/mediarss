package rss.deployer;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import rss.environment.Environment;

/**
 * User: dikmanm
 * Date: 21/02/2015 21:23
 */
@Component
public class Deployer {

    private static Logger LOGGER = LogManager.getLogger(Deployer.class);

    @Autowired
    private ApplicationContext applicationContext;

    public void createEnvironment() {
        LOGGER.info("Create environment");
        for (LifecycleDriver lifecycleDriver : applicationContext.getBeansOfType(LifecycleDriver.class).values()) {
            LOGGER.info("Call " + lifecycleDriver.getClass().getSimpleName() + ".tearDown()");
            lifecycleDriver.tearDown();
            LOGGER.info("Call " + lifecycleDriver.getClass().getSimpleName() + ".create()");
            lifecycleDriver.create();
        }
        LOGGER.info("Create environment - Finished");
    }

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        LOGGER.info("Starting executing lifecycle drivers");
        Deployer deployer = context.getBean(Deployer.class);
        if (System.getProperties().containsKey("createEnvironment")) {
            deployer.createEnvironment();
        }
        terminate();
        LOGGER.info("Finished executing lifecycle drivers");
    }

    // copied form AppConfigListener
    private static void terminate() {
        Environment.getInstance().shutdown();

        try {
            LOGGER.info("Sleeping 1000ms to finish shutting down");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOGGER.info(e.getMessage(), e);
        }
    }
}
