package rss.services.shows;

import rss.entities.Episode;
import rss.entities.Show;

import java.util.*;

/**
 * User: dikmanm
 * Date: 02/03/13 10:02
 */
public class DownloadScheduleResult {
    private Set<Episode> newEpisodes;
    private Collection<Show> failedShows;

    public DownloadScheduleResult() {
        failedShows = new ArrayList<>();
        newEpisodes = new HashSet<>();
    }

    public void addFailedShow(Show show) {
        this.failedShows.add(show);
    }

    public Collection<Episode> getNewEpisodes() {
        return newEpisodes;
    }

    public Collection<Show> getFailedShows() {
        return failedShows;
    }

    public void addNewEpisodes(List<Episode> episodes) {
        newEpisodes.addAll(episodes);
    }

    public void addNewEpisode(Episode episode) {
        newEpisodes.add(episode);
    }
}
