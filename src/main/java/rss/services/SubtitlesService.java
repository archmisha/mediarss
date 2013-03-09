package rss.services;

import rss.entities.Subtitles;
import rss.SubtitleLanguage;
import rss.entities.Episode;
import rss.entities.Torrent;

import java.util.Collection;

/**
 * User: dikmanm
 * Date: 24/01/13 22:38
 */
public interface SubtitlesService {

	void downloadTorrentSubtitles(Torrent torrent, SubtitleLanguage language);

	void downloadTorrentSubtitles(Torrent torrent, Collection<SubtitleLanguage> languages);

	void downloadEpisodeSubtitles(Torrent torrent, Episode episode, SubtitleLanguage language);

	void downloadEpisodeSubtitles(Torrent torrent, Episode episode, Collection<SubtitleLanguage> languages);

	com.turn.ttorrent.common.Torrent toTorrent(Subtitles subtitle);

	String getTrackerAnnounceUrl();
}
