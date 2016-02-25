package rss.content;

import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rss.environment.ServerMode;
import rss.log.LogService;
import rss.user.User;
import rss.user.UserService;

import java.util.Set;

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

    @Override
    public void load() {
        logService.info(DevContentLoader.class, "Loading DEV mode content");

        logService.info(DevContentLoader.class, "Loading default archmisha@gmail.com user");
        User user = userService.findByEmail("archmisha@gmail.com");
        if (user == null) {
            logService.info(DevContentLoader.class, "No default admin user is detected. Creating ...");
            userService.register("a", "b", "archmisha@gmail.com", "Aa123456", true);
        }
    }

    @Override
    public Set<ServerMode> getSupportedModes() {
        return Sets.newHashSet(ServerMode.DEV);
    }
}
