package rss.dao;

import rss.entities.Show;
import rss.entities.Subtitles;
import rss.entities.SubtitlesScanHistory;
import rss.entities.Torrent;
import rss.services.requests.subtitles.SubtitlesRequest;
import rss.services.subtitles.SubtitleLanguage;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface SubtitlesDao extends Dao<Subtitles> {

	List<Subtitles> find(SubtitlesRequest subtitlesRequest, SubtitleLanguage language);

	Collection<Subtitles> find(Set<Torrent> torrents, SubtitleLanguage subtitleLanguage);

	Collection<Subtitles> findByTorrent(Torrent torrent);

	List<SubtitleLanguage> getSubtitlesLanguagesForTorrent(Torrent torrent);

	List<SubtitleLanguage> getSubtitlesLanguages(Show show);

	SubtitlesScanHistory findSubtitleScanHistory(Torrent torrent, SubtitleLanguage language);

	Subtitles findByName(String name);
}
