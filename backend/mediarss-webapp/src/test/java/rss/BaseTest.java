package rss;

import org.apache.logging.log4j.LogManager;
import org.junit.Before;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import rss.log.LogService;

import java.io.File;

/**
 * User: Michael Dikman
 * Date: 24/12/12
 * Time: 20:55
 */
public class BaseTest {

    public static final String ERROR_KEY = "ERROR";

    @Mock
    private LogService logService;

    @Before
    public void setup() {
        System.setProperty(ERROR_KEY, "");
        setupLog4j();
        setupDataBaseProperties();
    }

    protected static void setupLog4j() {
        try {
            // no need for /WEB-INF/classes/ prefix
            File log4jPropsFile = new ClassPathResource("test-log4j.properties", BaseTest.class.getClassLoader()).getFile();
            String path = log4jPropsFile.getAbsolutePath();
            LogManager.getLogger(AppConfigListener.class).info("Log4j system initialized from " + path);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected static void setupDataBaseProperties() {
        System.setProperty("path-locator.txt", "test-path-locator.txt");
    }

}
