package rss.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rss.dao.UserDao;
import rss.entities.User;
import rss.log.LogService;
import rss.services.shows.ShowService;
import rss.services.user.UserService;

/**
 * User: dikmanm
 * Date: 16/08/2015 23:37
 */
@Component
public class TestContentLoader implements ContentLoader {

    @Autowired
    private LogService logService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ShowService showService;

    @Override
    public void load() {
        logService.info(TestContentLoader.class, "Loading TEST mode content");

        logService.info(TestContentLoader.class, "Loading default archmisha@gmail.com user");
        User user = userDao.findByEmail("archmisha@gmail.com");
        if (user == null) {
            logService.info(TestContentLoader.class, "No default admin user is detected. Creating ...");
            userService.register("a", "b", "archmisha@gmail.com", "Aa123456", true);
        }

        logService.info(TestContentLoader.class, "Loading shows list");
        showService.downloadShowList();
    }
}
