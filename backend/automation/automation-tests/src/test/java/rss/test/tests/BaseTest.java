package rss.test.tests;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import rss.test.Reporter;
import rss.test.services.TestPagesClient;
import rss.test.services.UserClient;
import rss.test.util.HttpUtils;
import rss.test.util.Unique;
import rss.test.util.WaitUtil;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * User: dikmanm
 * Date: 12/02/2015 22:44
 */
@ContextConfiguration(locations = {"classpath:/META-INF/spring/mediarss-tests-context.xml"})
public abstract class BaseTest extends AbstractJUnit4SpringContextTests {

    private static boolean isTomcatUp = false;
    private static boolean isTomcatStartFailed = false;

    @Rule
    public TestRule rule = new StartupRule();

    @Autowired
    protected Reporter reporter;

    @Autowired
    protected UserClient userService;

    @Autowired
    private TestPagesClient testPagesService;

    @Autowired
    protected Unique unique;

    @Autowired
    protected HttpUtils httpUtils;

    public boolean waitForTomcatStartup() {
        if (isTomcatStartFailed) {
            return false;
        }
        if (isTomcatUp) {
            return true;
        }

        reporter.info("Waiting for tomcat to start for 3 mins");
        WaitUtil.waitFor(WaitUtil.TIMEOUT_3_MIN, (int) TimeUnit.SECONDS.toMillis(5), new Runnable() {
            @Override
            public void run() {
                try {
                    String response = httpUtils.sendGetRequest("generate/?user=1&type=shows&feedId=1");
                    reporter.info("...");
                    assertTrue(response.length() > 0);
//                    assertEquals("Failed generating feed. Please contact support for assistance", response);
                    isTomcatUp = true;
                } catch (Exception e) {
                    isTomcatStartFailed = true;
                    reporter.info("Waiting for tomcat to start: " + e.getMessage());
                    throw e;
                }
            }
        });
        reporter.info("Tomcat is up");
        return true;
    }

    public class StartupRule implements TestRule {
        public Statement apply(Statement base, Description description) {
            return statement(base, description);
        }

        private Statement statement(final Statement base, final Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    if (!waitForTomcatStartup()) {
                        return;
                    }
                    try {
                        reporter.info("Reset automation-pages state");
                        testPagesService.resetOverrides();

                        reporter.info("Starting test: " + description.getClassName() + ":" + description.getMethodName());
                        userService.logout();
                        base.evaluate();
                    } catch (Throwable e) {
                        reporter.error(ExceptionUtils.getStackTrace(e));
                        throw e;
                    }
                }
            };
        }
    }
}
