package rss.test.shows;

import org.springframework.stereotype.Component;
import rss.shows.ShowAutoCompleteJSON;
import rss.shows.ShowJSON;
import rss.shows.ShowsScheduleJSON;
import rss.test.entities.TrackedShowsResult;
import rss.test.services.BaseService;
import rss.test.util.JsonTranslation;

import java.util.Collections;
import java.util.List;

/**
 * User: dikmanm
 * Date: 16/08/2015 14:43
 */
@Component
public class ShowsService extends BaseService {

    public ShowJSON getShow(String name) {
        reporter.info("Call show by name: " + name);
        String response = sendPostRequest("rest/shows/get", Collections.singletonMap("name", name));
        final ShowJSON[] shows = JsonTranslation.jsonString2Object(response, ShowJSON[].class);
        return shows.length == 0 ? null : shows[0];
    }

    public List<ShowJSON> getTrackedShows() {
        reporter.info("Call get tracked shows");
        String response = sendGetRequest("rest/shows/tracked-shows");
        return JsonTranslation.jsonString2Object(response, TrackedShowsResult.class).getTrackedShows();
    }

    public void addTrackedShow(ShowJSON show) {
        reporter.info("Call add tracked show: " + show.getName());
        sendPostRequest("rest/shows/add-tracked/" + show.getId(), null);
    }

    public void removeTrackedShow(ShowJSON show) {
        reporter.info("Call remove tracked show: " + show.getName());
        sendPostRequest("rest/shows/remove-tracked/" + show.getId(), null);
    }

    public ShowsScheduleJSON getSchedule() {
        reporter.info("Call get schedule");
        String response = sendGetRequest("rest/shows/schedule");
        return JsonTranslation.jsonString2Object(response, ShowsScheduleJSON.class);
    }

    public ShowAutoCompleteJSON autoCompleteTracked(String term) {
        reporter.info("Call auto-complete tracked shows with term '" + term + "'");
        String response = sendGetRequest("rest/shows/tracked/autocomplete", Collections.singletonMap("term", term));
        return JsonTranslation.jsonString2Object(response, ShowAutoCompleteJSON.class);
    }
}
