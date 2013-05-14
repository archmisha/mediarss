package rss.dao;

import rss.entities.Show;
import rss.entities.Subtitles;
import rss.services.subtitles.SubtitleLanguage;
import rss.entities.Torrent;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface SubtitlesDao extends Dao<Subtitles> {

	Subtitles find(Torrent torrent, SubtitleLanguage language);

	Collection<Subtitles> find(Set<Torrent> torrents, SubtitleLanguage ... subtitleLanguages);

	Collection<Subtitles> findByTorrent(Torrent torrent);

	List<SubtitleLanguage> getSubtitlesLanguagesForTorrent(Torrent torrent);

	List<SubtitleLanguage> getSubtitlesLanguages(Show show);
}
