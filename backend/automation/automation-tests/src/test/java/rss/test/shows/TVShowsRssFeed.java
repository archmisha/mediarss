package rss.test.shows;

import org.springframework.stereotype.Component;
import rss.shows.ShowJSON;
import rss.test.entities.UserData;
import rss.test.services.BaseService;
import rss.test.util.JsonTranslation;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dikmanm on 24/10/2015.
 */
@Component
public class TVShowsRssFeed extends BaseService {

    public ShowJSON getFeed(UserData user) {
        reporter.info("Call get tv shows rss feed");
        Map<String, String> params = new HashMap<>();
        params.put("type", "shows");
        params.put("user", String.valueOf(user.getId()));
        String response = sendGetRequest("generate", params);
        final ShowJSON[] shows = JsonTranslation.jsonString2Object(response, ShowJSON[].class);
        return shows.length == 0 ? null : shows[0];
    }
}
