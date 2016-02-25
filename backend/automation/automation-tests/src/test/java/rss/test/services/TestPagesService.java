package rss.test.services;

import org.springframework.stereotype.Component;
import rss.shows.ShowJSON;
import rss.shows.thetvdb.TheTvDbEpisode;
import rss.shows.thetvdb.TheTvDbShow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 17/08/2015 09:37
 */
@Component
public class TestPagesService extends BaseService {

    public void createShow(TheTvDbShow theTvDbShow) {
        sendPostRequest("test-pages/rest/test-pages/shows/show", theTvDbShow);
    }

    public void createEpisode(TheTvDbEpisode theTvDbEpisode) {
        sendPostRequest("test-pages/rest/test-pages/shows/episode", theTvDbEpisode);
    }

    public void setShowEnded(TheTvDbShow show) {
        reporter.info("Calling set show ended for show '" + show.getName() + "'");
        sendPostRequest("test-pages/rest/test-pages/shows/set-ended/" + show.getId(), Collections.emptyMap());
    }

    public void resetOverrides() {
        sendGetRequest("test-pages/rest/test-pages/resetOverrides");
    }

    public void addTorrent(ShowJSON show, int season, int episode) {
        Map<String, String> params = new HashMap<>();
        params.put("show", show.getName());
        params.put("season", String.valueOf(season));
        params.put("episode", String.valueOf(episode));
        sendPostRequest("test-pages/rest/test-pages/torrents", params);
    }
}
