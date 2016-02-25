package rss.test.shows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rss.shows.thetvdb.TheTvDbEpisode;
import rss.shows.thetvdb.TheTvDbShow;
import rss.test.services.TestPagesService;
import rss.test.services.Unique;
import rss.test.util.JsonTranslation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * User: dikmanm
 * Date: 22/08/2015 02:18
 */
@Component
public class TheTvDbEpisodeBuilder {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private Unique unique;

    @Autowired
    private TestPagesService testPagesService;

    private TheTvDbEpisode theTvDbEpisode;

    public TheTvDbEpisodeBuilder anEpisode(TheTvDbShow theTvDbShow, int season, int episode) {
        theTvDbEpisode = new TheTvDbEpisode();
        theTvDbEpisode.setId(unique.randomInt());
        theTvDbEpisode.setSeason(season);
        theTvDbEpisode.setEpisode(episode);
        theTvDbEpisode.setShowId(theTvDbShow.getId());
        Date date = new Date();
        date.setTime(date.getTime() - TimeUnit.DAYS.toMillis(1));
        theTvDbEpisode.setAirDate(sdf.format(date));
        return this;
    }

    public TheTvDbEpisodeBuilder withAirDate(Date airDate) {
        theTvDbEpisode.setAirDate(sdf.format(airDate));
        return this;
    }

    public TheTvDbEpisode build() {
        TheTvDbEpisode copy = JsonTranslation.jsonString2Object(JsonTranslation.object2JsonString(theTvDbEpisode), TheTvDbEpisode.class);
        testPagesService.createEpisode(copy);
        return copy;
    }
}
