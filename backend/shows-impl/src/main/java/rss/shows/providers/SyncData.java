package rss.shows.providers;

import rss.torrents.Episode;
import rss.torrents.Show;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by dikmanm on 31/10/2015.
 */
public class SyncData {
    private List<Show> shows;
    private List<Episode> episodes;

    public SyncData() {
        shows = new ArrayList<>();
        episodes = new ArrayList<>();
    }

    public List<Show> getShows() {
        return shows;
    }

    public List<Episode> getEpisodes() {
        return episodes;
    }

    public void addEpisode(Episode episode) {
        this.episodes.add(episode);
    }

    public void addShow(Show show) {
        this.shows.add(show);
    }

    public void addEpisodes(Collection<Episode> episodes) {
        this.episodes.addAll(episodes);
    }
}
