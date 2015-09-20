package rss.test.shows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rss.shows.tvrage.TVRageConstants;
import rss.shows.tvrage.TVRageShow;
import rss.test.services.TestPagesService;
import rss.test.services.Unique;
import rss.test.util.JsonTranslation;

/**
 * User: dikmanm
 * Date: 21/08/2015 18:59
 */
@Component
public class TVRageShowBuilder {
    @Autowired
    private Unique unique;

    @Autowired
    private TestPagesService testPagesService;

    private TVRageShow tvRageShow;

    private void aShow() {
        tvRageShow = new TVRageShow();
        tvRageShow.setId(unique.randomInt());
    }

    public TVRageShowBuilder aRunningShow() {
        aShow();
        tvRageShow.setStatus(TVRageConstants.ShowListStatus.RETURNING_STATUS);
        return this;
    }

    public TVRageShowBuilder anEndedShow() {
        aShow();
        tvRageShow.setStatus(TVRageConstants.ShowListStatus.ENDED_STATUS);
        return this;
    }

    public TVRageShowBuilder withName(String name) {
        tvRageShow.setName(name);
        return this;
    }

    public TVRageShow build() {
        TVRageShow copy = JsonTranslation.jsonString2Object(JsonTranslation.object2JsonString(tvRageShow), TVRageShow.class);
        copy.setName(unique.appendUnique(copy.getName()));
        testPagesService.createShow(copy);
        return copy;
    }
}
