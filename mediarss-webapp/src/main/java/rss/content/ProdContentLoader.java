package rss.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rss.log.LogService;
import rss.services.OOTBContentLoader;

/**
 * User: dikmanm
 * Date: 16/08/2015 23:37
 */
@Component
public class ProdContentLoader implements ContentLoader {

    @Autowired
    private LogService logService;

    @Autowired
    private OOTBContentLoader ootbContentLoader;

    @Override
    public void load() {
        logService.info(ProdContentLoader.class, "Loading PROD mode content");

        try {
            logService.info(ProdContentLoader.class, "Loading TVRage shows");
            ootbContentLoader.loadTVRageShows();
        } catch (Exception e) {
            logService.error(ProdContentLoader.class, "Failed loading OOTB content: " + e.getMessage(), e);
        }
    }
}
