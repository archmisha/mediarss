package rss.shows.providers;

import rss.torrents.Episode;
import rss.torrents.Show;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by dikmanm on 31/10/2015.
 */
public class ShowData {

    private Show show;
    private Collection<Episode> episodes;

    public ShowData(Show show) {
        this.show = show;
        this.episodes = new ArrayList<>();
    }

    public Collection<Episode> getEpisodes() {
        return episodes;
    }

    public void addEpisode(Episode episode) {
        this.episodes.add(episode);
    }

    public Show getShow() {
        return show;
    }
}
