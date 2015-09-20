package rss.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rss.dao.UserDao;
import rss.entities.User;
import rss.log.LogService;
import rss.services.OOTBContentLoader;
import rss.services.user.UserService;

/**
 * User: dikmanm
 * Date: 16/08/2015 23:37
 */
@Component
public class DevContentLoader implements ContentLoader {

    @Autowired
    private LogService logService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private OOTBContentLoader ootbContentLoader;

    @Override
    public void load() {
        logService.info(DevContentLoader.class, "Loading DEV mode content");

        try {
            logService.info(DevContentLoader.class, "Loading TVRage shows");
            ootbContentLoader.loadTVRageShows();
        } catch (Exception e) {
            logService.error(DevContentLoader.class, "Failed loading OOTB content: " + e.getMessage(), e);
        }

        logService.info(DevContentLoader.class, "Loading default archmisha@gmail.com user");
        User user = userDao.findByEmail("archmisha@gmail.com");
        if (user == null) {
            logService.info(DevContentLoader.class, "No default admin user is detected. Creating ...");
            userService.register("a", "b", "archmisha@gmail.com", "Aa123456", true);
        }
    }
}
