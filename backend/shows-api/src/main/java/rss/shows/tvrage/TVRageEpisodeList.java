package rss.shows.tvrage;

import java.util.List;

/**
 * User: dikmanm
 * Date: 17/08/2015 14:33
 */
public class TVRageEpisodeList {
    private List<TVRageSeason> seasons;
    private TVRageMovie movie;
    private TVRageSpecial special;

    public List<TVRageSeason> getSeasons() {
        return seasons;
    }

    public void setSeasons(List<TVRageSeason> seasons) {
        this.seasons = seasons;
    }
}
