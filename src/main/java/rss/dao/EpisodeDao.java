package rss.dao;

import rss.services.EpisodeRequest;
import rss.entities.Torrent;
import rss.SubtitleLanguage;
import rss.entities.Episode;

import java.util.Collection;
import java.util.List;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface EpisodeDao extends Dao<Episode> {

	List<Episode> find(Collection<EpisodeRequest> episodes);

    Episode find(EpisodeRequest episodeRequest);

	List<SubtitleLanguage> getSubtitlesLanguages(Episode episode);

	Episode find(Torrent torrent);
}
