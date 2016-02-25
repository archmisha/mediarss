package rss.shows.providers;

import rss.torrents.Show;

import java.util.Collection;

/**
 * User: Michael Dikman
 * Date: 24/12/12
 * Time: 23:52
 */
public interface ShowsProvider {

    Show search(String name);

    SyncData getSyncData();

    Collection<Show> downloadShowList();

    ShowData getShowData(Show show);
}
