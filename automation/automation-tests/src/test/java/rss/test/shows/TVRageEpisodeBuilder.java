package rss.test.shows;

import org.springframework.stereotype.Component;
import rss.shows.tvrage.TVRageEpisode;
import rss.test.util.JsonTranslation;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: dikmanm
 * Date: 22/08/2015 02:18
 */
@Component
public class TVRageEpisodeBuilder {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private TVRageEpisode tvRageEpisode;

    public TVRageEpisodeBuilder anEpisode(int number) {
        this.tvRageEpisode = new TVRageEpisode();
        this.tvRageEpisode.setSeasonnum(String.valueOf(number));
        return this;
    }

    public TVRageEpisodeBuilder withAirDate(Date airDate) {
        this.tvRageEpisode.setAirdate(sdf.format(airDate));
        return this;
    }

    public TVRageEpisode build() {
        return JsonTranslation.jsonString2Object(JsonTranslation.object2JsonString(tvRageEpisode), TVRageEpisode.class);
    }
}
