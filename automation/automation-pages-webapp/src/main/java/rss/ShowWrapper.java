package rss;

import rss.shows.thetvdb.TheTvDbShow;

/**
 * Created by dikmanm on 02/02/2016.
 */
public class ShowWrapper {

    private Long updateTime;
    private TheTvDbShow show;

    public ShowWrapper(TheTvDbShow show, Long updateTime) {
        this.updateTime = updateTime;
        this.show = show;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public TheTvDbShow getShow() {
        return show;
    }
}
