package rss.subtitles.dao;

import rss.ems.dao.Dao;
import rss.torrents.Show;
import rss.torrents.Subtitles;
import rss.torrents.Torrent;
import rss.torrents.requests.subtitles.SubtitlesRequest;
import rss.user.subtitles.SubtitleLanguage;

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

	SubtitlesScanHistoryImpl findSubtitleScanHistory(Torrent torrent, SubtitleLanguage language);

	Subtitles findByName(String name);
}
