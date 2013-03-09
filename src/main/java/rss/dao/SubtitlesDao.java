package rss.dao;

import rss.entities.Subtitles;
import rss.SubtitleLanguage;
import rss.entities.Torrent;

import java.util.Collection;
import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface SubtitlesDao extends Dao<Subtitles> {

	Subtitles find(Torrent torrent, SubtitleLanguage language);

	Collection<Subtitles> find(Set<Torrent> torrents, SubtitleLanguage ... subtitleLanguages);
}
