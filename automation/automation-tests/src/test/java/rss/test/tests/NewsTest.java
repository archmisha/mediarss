package rss.test.tests;

import com.google.common.base.Function;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import rss.test.entities.News;
import rss.test.entities.UserData;
import rss.test.services.AdminService;
import rss.test.util.AssertUtils;

import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;

/**
 * User: dikmanm
 * Date: 12/02/2015 22:44
 */
public class NewsTest extends BaseTest {

    @Autowired
    private AdminService adminService;

    @Test
    public void testCreateNews() {
        reporter.info("Create admin user and login");
        UserData adminUser = userService.createAdminUser();
        userService.login(adminUser);

        reporter.info("Create news entity, login twice and see news on both times");
        long news1 = adminService.createNews("news1");
        assertTrue(AssertUtils.contains(Arrays.asList(userService.login(adminUser).getNews()), newsToIdFunc(), news1));
        assertTrue(AssertUtils.contains(Arrays.asList(userService.login(adminUser).getNews()), newsToIdFunc(), news1));

        reporter.info("Create another news entity, login and see both news sorted by date");
        long news2 = adminService.createNews("news2");
        assertTrue(AssertUtils.contains(Arrays.asList(userService.login(adminUser).getNews()), newsToIdFunc(), news1, news2));

        reporter.info("Dismiss all news entities, login and have no news");
        adminService.dismissNews();
        assertTrue(userService.login(adminUser).getNews().length == 0);
    }

    // todo test permissions of create and disminss news

    private Function<News, Long> newsToIdFunc() {
        return new Function<News, Long>() {
            @Override
            public Long apply(News news) {
                return news.getId();
            }
        };
    }
}
