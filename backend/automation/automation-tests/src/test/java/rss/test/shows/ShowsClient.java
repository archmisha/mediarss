package rss.test.shows;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import rss.shows.SearchResultJSON;
import rss.shows.ShowAutoCompleteJSON;
import rss.shows.ShowJSON;
import rss.shows.schedule.ShowsScheduleJSON;
import rss.test.entities.TrackedShowsResult;
import rss.test.services.BaseClient;
import rss.test.util.json.JsonTranslation;
import rss.test.util.WaitUtil;

import java.util.*;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * User: dikmanm
 * Date: 16/08/2015 14:43
 */
@Component
public class ShowsClient extends BaseClient {

    @Override
    protected String getServiceName() {
        return "shows";
    }

    public ShowJSON getShow(String name) {
        reporter.info("Call show by name: " + name);
        String response = httpUtils.sendPostRequest(getBasePath() + "/get", Collections.singletonMap("name", name));
        final ShowJSON[] shows = JsonTranslation.jsonString2Object(response, ShowJSON[].class);
        return shows.length == 0 ? null : shows[0];
    }

    public List<ShowJSON> getTrackedShows() {
        reporter.info("Call get tracked shows");
        String response = httpUtils.sendGetRequest(getBasePath() + "/tracked-shows");
        return JsonTranslation.jsonString2Object(response, TrackedShowsResult.class).getTrackedShows();
    }

    public void addTrackedShow(ShowJSON show) {
        reporter.info("Call add tracked show: " + show.getName());
        httpUtils.sendPostRequest(getBasePath() + "/add-tracked/" + show.getId(), null);
    }

    public void removeTrackedShow(ShowJSON show) {
        reporter.info("Call remove tracked show: " + show.getName());
        httpUtils.sendPostRequest(getBasePath() + "/remove-tracked/" + show.getId(), null);
    }

    public ShowsScheduleJSON getSchedule() {
        reporter.info("Call get schedule");
        String response = httpUtils.sendGetRequest(getBasePath() + "/schedule");
        return JsonTranslation.jsonString2Object(response, ShowsScheduleJSON.class);
    }

    public ShowAutoCompleteJSON autoCompleteTracked(String term) {
        reporter.info("Call auto-complete tracked shows with term '" + term + "'");
        String response = httpUtils.sendGetRequest(getBasePath() + "/tracked/autocomplete", Collections.singletonMap("term", term));
        return JsonTranslation.jsonString2Object(response, ShowAutoCompleteJSON.class);
    }

    public SearchResultJSON search(ShowJSON show, int season, int episode) {
        return search(show, season, episode, false);
    }

    public SearchResultJSON search(String name) {
        ShowJSON showJSON = new ShowJSON();
        showJSON.withName(name);
        return search(showJSON, null, null, false);
    }

    public SearchResultJSON search(ShowJSON show, Integer season, Integer episode, boolean forceDownload) {
        reporter.info(String.format("Call search for show %s (%d) season %d episode %d", show.getName(), show.getId(), season, episode));
        Map<String, String> params = new HashMap<>();
        params.put("showId", String.valueOf(show.getId()));
        params.put("title", String.valueOf(show.getName()));
        if (season != null) {
            params.put("season", String.valueOf(season));
        }
        if (episode != null) {
            params.put("episode", String.valueOf(episode));
        }
        params.put("forceDownload", String.valueOf(forceDownload));
        String response = httpUtils.sendGetRequest(getBasePath() + "/search", params);
        return JsonTranslation.jsonString2Object(response, SearchResultJSON.class);
    }

    public void downloadEpisode(long torrentId) {
        reporter.info("Call download episode: " + torrentId);
        httpUtils.sendGetRequest(getBasePath() + "/episode/download", Collections.singletonMap("torrentId", String.valueOf(torrentId)));
    }

    public void downloadEpisodes(Collection<String> torrentIds) {
        String param = StringUtils.join(torrentIds.toArray(new String[torrentIds.size()]), ",");
        reporter.info("Call download episodes: " + param);
        httpUtils.sendGetRequest(getBasePath() + "/episode/download-all", Collections.singletonMap("torrentIds", param));
    }

    public List<SearchResultJSON> getSearchStatus() {
        reporter.info("Call get search status");
        String response = httpUtils.sendGetRequest(getBasePath() + "/search/status");
        return Arrays.asList(JsonTranslation.jsonString2Object(response, SearchResultJSON[].class));
    }

    public void removeSearchStatus(String searchId) {
        reporter.info("Call remove search status with id " + searchId);
        httpUtils.sendGetRequest(getBasePath() + "/search/remove/" + searchId);
    }

    public SearchResultJSON getSearchStatusSingleWithPolling() {
        return WaitUtil.waitFor(new Callable<SearchResultJSON>() {
            @Override
            public SearchResultJSON call() throws Exception {
                List<SearchResultJSON> searchStatus = getSearchStatus();
                assertEquals(1, searchStatus.size());
                SearchResultJSON searchResult = searchStatus.iterator().next();
                assertNotNull(searchResult.getStart());
                assertNotNull(searchResult.getEnd());
                return searchResult;
            }
        });
    }
}
