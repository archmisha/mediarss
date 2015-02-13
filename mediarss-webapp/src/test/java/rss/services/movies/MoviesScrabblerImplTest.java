package rss.services.movies;

import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import rss.BaseTest;
import rss.services.JobRunner;

/**
 * User: Michael Dikman
 * Date: 23/12/12
 * Time: 22:32
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testContext.xml"})
@Transactional
@Ignore
public class MoviesScrabblerImplTest extends BaseTest {

    @Autowired
    private MoviesScrabbler moviesScrobbler;

    @Test
    public void testRun() {
        ((JobRunner)moviesScrobbler).start();

        if (StringUtils.isNotBlank(System.getProperty(ERROR_KEY))) {
            Assert.fail(System.getProperty(ERROR_KEY));
        }
    }
}
