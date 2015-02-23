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
import rss.test.services.BaseService;

/**
 * User: dikmanm
 * Date: 12/02/2015 22:44
 */
@ContextConfiguration(locations = {"classpath:/META-INF/spring/mediarss-tests-context.xml"})
public abstract class BaseTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    protected Reporter reporter;

    @Autowired
    protected BaseService baseService;

    @Rule
    public TestRule rule = new StartupRule();

    public class StartupRule implements TestRule {
        public Statement apply(Statement base, Description description) {
            return statement(base, description);
        }

        private Statement statement(final Statement base, final Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    baseService.waitForTomcatStartup();
                    try {
                        reporter.info("Starting test: " + description.getClassName() + ":" + description.getMethodName());
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
