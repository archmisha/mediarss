package rss.shows.tvrage;

import java.util.List;

/**
 * User: dikmanm
 * Date: 17/08/2015 14:32
 */
public class TVRageSeason {
    private int no;
    private List<TVRageEpisode> episodes;

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public List<TVRageEpisode> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<TVRageEpisode> episodes) {
        this.episodes = episodes;
    }
}
