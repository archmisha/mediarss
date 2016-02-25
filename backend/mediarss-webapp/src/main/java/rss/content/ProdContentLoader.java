package rss.content;

import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rss.environment.ServerMode;
import rss.log.LogService;

import java.util.Set;

/**
 * User: dikmanm
 * Date: 16/08/2015 23:37
 */
@Component
public class ProdContentLoader implements ContentLoader {

    @Autowired
    private LogService logService;

    @Override
    public void load() {
        logService.info(ProdContentLoader.class, "Loading PROD mode content");
    }

    @Override
    public Set<ServerMode> getSupportedModes() {
        return Sets.newHashSet(ServerMode.PROD);
    }
}
