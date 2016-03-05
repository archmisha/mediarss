package rss.automation.pages;

import rss.shows.thetvdb.TheTvDbEpisode;

/**
 * Created by dikmanm on 02/02/2016.
 */
public class EpisodeWrapper {
    private Long updateTime;
    private TheTvDbEpisode episode;

    public EpisodeWrapper(TheTvDbEpisode episode, Long updateTime) {
        this.updateTime = updateTime;
        this.episode = episode;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public TheTvDbEpisode getEpisode() {
        return episode;
    }
}
