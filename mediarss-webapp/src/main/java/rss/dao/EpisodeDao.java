package rss.dao;

import rss.entities.Episode;
import rss.entities.Show;
import rss.entities.Torrent;
import rss.entities.User;
import rss.services.requests.episodes.ShowRequest;

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
