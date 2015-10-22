package rss.subtitles;

import rss.torrents.Show;
import rss.torrents.Subtitles;
import rss.torrents.Torrent;
import rss.torrents.requests.subtitles.SubtitlesRequest;
import rss.user.subtitles.SubtitleLanguage;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * User: dikmanm
 * Date: 24/01/13 22:38
 */
public interface SubtitlesService {

	void downloadMissingSubtitles();

	void downloadSubtitlesAsync(Set<SubtitlesRequest> subtitlesRequests);

	Collection<Subtitles> find(SubtitlesRequest request, SubtitleLanguage subtitleLanguage);

	SubtitlesScanHistory findSubtitleScanHistory(Torrent torrent, SubtitleLanguage subtitleLanguage);

	Subtitles findByName(String name);

	void deleteSubtitlesByTorrent(Torrent torrent);

	List<SubtitleLanguage> getSubtitlesLanguages(Show show);

	void persist(Subtitles subtitles);

	SubtitlesServiceFactory factory();

	Collection<Subtitles> find(Set<Torrent> torrents, SubtitleLanguage subtitleLanguage);
}
