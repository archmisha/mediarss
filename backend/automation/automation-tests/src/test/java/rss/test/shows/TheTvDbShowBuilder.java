package rss.test.shows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rss.shows.thetvdb.TheTvDbConstants;
import rss.shows.thetvdb.TheTvDbShow;
import rss.test.services.TestPagesClient;
import rss.test.util.Unique;
import rss.test.util.json.JsonTranslation;

/**
 * User: dikmanm
 * Date: 21/08/2015 18:59
 */
@Component
public class TheTvDbShowBuilder {
    @Autowired
    private Unique unique;

    @Autowired
    private TestPagesClient testPagesService;

    private TheTvDbShow theTvDbShow;

    private void aShow() {
        theTvDbShow = new TheTvDbShow();
        theTvDbShow.setId(unique.randomInt());
    }

    public TheTvDbShowBuilder aRunningShow() {
        aShow();
        theTvDbShow.setStatus("");
        return this;
    }

    public TheTvDbShowBuilder anEndedShow() {
        aShow();
        theTvDbShow.setStatus(TheTvDbConstants.ENDED_STATUS);
        return this;
    }

    public TheTvDbShowBuilder withName(String name) {
        theTvDbShow.setName(name);
        return this;
    }

    public TheTvDbShow build() {
        TheTvDbShow copy = JsonTranslation.jsonString2Object(JsonTranslation.object2JsonString(theTvDbShow), TheTvDbShow.class);
        copy.setName(unique.appendUnique(copy.getName()));
        testPagesService.createShow(copy);
        return copy;
    }
}
