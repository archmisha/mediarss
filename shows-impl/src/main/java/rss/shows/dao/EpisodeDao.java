package rss.shows.dao;

import rss.ems.dao.Dao;
import rss.shows.entities.Episode;
import rss.shows.entities.Show;
import rss.torrents.Torrent;
import rss.torrents.requests.shows.ShowRequest;

import java.util.Collection;
import java.util.List;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface EpisodeDao extends Dao<Episode> {

	List<Episode> findByRequests(Collection<ShowRequest> showRequests);

	List<Episode> find(ShowRequest episodeRequest);

	Episode find(Torrent torrent);

	Collection<Episode> getEpisodesForSchedule(User user);

	boolean exists(Show show, Episode episode);
}
