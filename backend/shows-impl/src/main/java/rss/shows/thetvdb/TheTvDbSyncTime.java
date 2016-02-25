package rss.shows.thetvdb;

import rss.rms.RmsResource;

/**
 * Created by dikmanm on 29/10/2015.
 */
public class TheTvDbSyncTime implements RmsResource {
    private String id;
    private long time;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
