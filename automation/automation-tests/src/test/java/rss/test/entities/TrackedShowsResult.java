package rss.test.entities;

import rss.shows.ShowJSON;

import java.util.List;

/**
 * User: dikmanm
 * Date: 16/08/2015 16:00
 */
public class TrackedShowsResult {

    private List<ShowJSON> trackedShows;

    public List<ShowJSON> getTrackedShows() {
        return trackedShows;
    }

    public void setTrackedShows(List<ShowJSON> trackedShows) {
        this.trackedShows = trackedShows;
    }
}
