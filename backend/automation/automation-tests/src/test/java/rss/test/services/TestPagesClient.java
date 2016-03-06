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
public class TestPagesClient extends BaseClient {

    @Override
    protected String getServiceName() {
        return "test-pages";
    }

    @Override
    protected String getContextPath() {
        return System.getProperty("server.automation.context");
    }

    @Override
    protected String getBasePath() {
        return "http://localhost:" + System.getProperty("server.automation.port") + "/" + super.getBasePath();
    }

    public void createShow(TheTvDbShow theTvDbShow) {
        httpUtils.sendPostRequest(getBasePath() + "/shows/show", theTvDbShow);
    }

    public void createEpisode(TheTvDbEpisode theTvDbEpisode) {
        httpUtils.sendPostRequest(getBasePath() + "/shows/episode", theTvDbEpisode);
    }

    public void setShowEnded(TheTvDbShow show) {
        reporter.info("Calling set show ended for show '" + show.getName() + "'");
        httpUtils.sendPostRequest(getBasePath() + "/shows/set-ended/" + show.getId(), Collections.emptyMap());
    }

    public void resetOverrides() {
        httpUtils.sendGetRequest(getBasePath() + "/resetOverrides");
    }

    public void addTorrent(ShowJSON show, int season, int episode) {
        Map<String, String> params = new HashMap<>();
        params.put("show", show.getName());
        params.put("season", String.valueOf(season));
        params.put("episode", String.valueOf(episode));
        httpUtils.sendPostRequest(getBasePath() + "/torrents", params);
    }
}
