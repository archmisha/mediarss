package rss.shows;

import rss.torrents.Episode;
import rss.torrents.Show;

import java.util.Collection;

/**
 * User: dikmanm
 * Date: 16/10/2015 08:39
 */
public class TheTVDbServiceImpl implements ShowsProvider {

    private static final String API_KEY = "EB8D0878240F2DD7";

    @Override
    public Show search(String name) {
        return null;
    }

    @Override
    public Collection<Show> downloadShowList() {
        return null;
    }

    @Override
    public Collection<Episode> downloadSchedule() {
        return null;
    }

    @Override
    public Collection<Episode> downloadSchedule(Show show) {
        return null;
    }
}
