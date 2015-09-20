package rss.test.shows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rss.shows.tvrage.TVRageEpisodeList;
import rss.shows.tvrage.TVRageSeason;
import rss.shows.tvrage.TVRageShow;
import rss.shows.tvrage.TVRageShowInfo;
import rss.test.services.TestPagesService;
import rss.test.services.Unique;
import rss.test.util.JsonTranslation;

import java.util.Arrays;

/**
 * User: dikmanm
 * Date: 21/08/2015 18:59
 */
@Component
public class TVRageShowInfoBuilder {
    @Autowired
    private Unique unique;

    @Autowired
    private TestPagesService testPagesService;

    private TVRageShowInfo tvRageShowInfo;

    public TVRageShowInfoBuilder anInfo(TVRageShow show) {
        tvRageShowInfo = new TVRageShowInfo();
        tvRageShowInfo.setShowid(show.getId());
        return this;
    }

    public TVRageShowInfoBuilder withEpisodes(TVRageSeason... seasons) {
        TVRageEpisodeList episodelist = new TVRageEpisodeList();
        episodelist.setSeasons(Arrays.asList(seasons));
        tvRageShowInfo.setEpisodelist(episodelist);
        return this;
    }

    public TVRageShowInfo build() {
        TVRageShowInfo copy = JsonTranslation.jsonString2Object(JsonTranslation.object2JsonString(tvRageShowInfo), TVRageShowInfo.class);
        testPagesService.createShowInfo(copy);
        return copy;
    }
}
