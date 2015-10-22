package rss.test.services;

import org.springframework.stereotype.Component;
import rss.shows.ShowJSON;
import rss.shows.tvrage.TVRageShow;
import rss.shows.tvrage.TVRageShowInfo;

import java.util.Collections;

/**
 * User: dikmanm
 * Date: 17/08/2015 09:37
 */
@Component
public class TestPagesService extends BaseService {

    public void createShow(TVRageShow tvRageShow) {
        sendPostRequest("test-pages/rest/test-pages/shows/show", tvRageShow);
    }

    public void createShowInfo(TVRageShowInfo tvRageShowInfo) {
        sendPostRequest("test-pages/rest/test-pages/shows/info", tvRageShowInfo);
    }

    public void setShowEnded(ShowJSON show) {
        sendPostRequest("test-pages/rest/test-pages/shows/set-ended/" + show.getTvRageId(), Collections.emptyMap());
    }

    public void resetOverrides() {
        sendGetRequest("test-pages/rest/test-pages/resetOverrides");
    }
}
