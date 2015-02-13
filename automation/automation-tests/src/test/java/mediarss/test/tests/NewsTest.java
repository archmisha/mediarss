package mediarss.test.tests;

import mediarss.test.entities.UserData;
import mediarss.test.services.AdminService;
import mediarss.test.services.UserService;
import mediarss.test.util.AssertUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;

/**
 * User: dikmanm
 * Date: 12/02/2015 22:44
 */
public class NewsTest extends BaseTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @Test
    public void testCreateNews() {
        reporter.info("Create admin user and login");
        UserData adminUser = UserData.createAdminUser("user1", "user1");
        userService.createUser(adminUser);
        userService.login(adminUser);

        reporter.info("Create news entity, login twice and see news on both times");
        long news1 = adminService.createNews("news1");
        assertTrue(AssertUtils.contains(Arrays.asList(userService.login(adminUser).getNews()), news1));
        assertTrue(AssertUtils.contains(Arrays.asList(userService.login(adminUser).getNews()), news1));

        reporter.info("Create another news entity, login and see both news sorted by date");
        long news2 = adminService.createNews("news2");
        assertTrue(AssertUtils.contains(Arrays.asList(userService.login(adminUser).getNews()), news1, news2));

        reporter.info("Dismiss previous news entity, login and see only the new news entity");
        adminService.dismissNews(news1);
        assertTrue(AssertUtils.contains(Arrays.asList(userService.login(adminUser).getNews()), news2));
    }
}
