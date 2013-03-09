package rss.services.shows;

import rss.entities.Episode;
import rss.entities.Show;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * User: Michael Dikman
 * Date: 24/12/12
 * Time: 23:52
 */
public interface ShowsProvider {

    Show search(String name);

//    int getEpisodesCount(Show show, int season);

	Collection<Episode> downloadSchedule();

	Collection<Show> downloadShowList();

	Collection<Episode> downloadInfo(Show show);

//	int getSeasonCount(Show show);

//	Show downloadShowByUrl(String url);
}
