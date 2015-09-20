package rss.test.shows;

import org.springframework.stereotype.Component;
import rss.shows.tvrage.TVRageEpisode;
import rss.shows.tvrage.TVRageSeason;
import rss.test.util.JsonTranslation;

import java.util.Arrays;

/**
 * User: dikmanm
 * Date: 22/08/2015 02:13
 */
@Component
public class TVRageSeasonBuilder {

    private TVRageSeason season;

    public TVRageSeasonBuilder aSeason(int number) {
        this.season = new TVRageSeason();
        this.season.setNo(number);
        return this;
    }

    public TVRageSeasonBuilder withEpisodes(TVRageEpisode... episodes) {
        season.setEpisodes(Arrays.asList(episodes));
        return this;
    }

    public TVRageSeason build() {
        return JsonTranslation.jsonString2Object(JsonTranslation.object2JsonString(season), TVRageSeason.class);
    }
}
