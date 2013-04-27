package rss.dao;

import rss.entities.Show;
import rss.services.requests.EpisodeRequest;
import rss.entities.Torrent;
import rss.SubtitleLanguage;
import rss.entities.Episode;
import rss.services.requests.ShowRequest;

import java.util.Collection;
import java.util.List;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface EpisodeDao extends Dao<Episode> {

	List<Episode> find(Collection<ShowRequest> episodes);

	List<Episode> find(EpisodeRequest episodeRequest);

	List<SubtitleLanguage> getSubtitlesLanguages(Episode episode);

	Episode find(Torrent torrent);

	Collection<Episode> getEpisodesForSchedule(List<Long> showIds);

	boolean exists(Show show, Episode episode);
}
