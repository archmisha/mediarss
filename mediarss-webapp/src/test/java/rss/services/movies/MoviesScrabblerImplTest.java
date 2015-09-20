package rss.services.movies;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import rss.BaseTest;

/**
 * User: Michael Dikman
 * Date: 23/12/12
 * Time: 22:32
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:testContext.xml"})
@Transactional
@Ignore
public class MoviesScrabblerImplTest extends BaseTest {

    @Autowired
    private MoviesScrabblerImpl moviesScrobbler;

    @Test
    public void testRun() {
        moviesScrobbler.run();

        if (StringUtils.isNotBlank(System.getProperty(ERROR_KEY))) {
            Assert.fail(System.getProperty(ERROR_KEY));
        }
    }
}
