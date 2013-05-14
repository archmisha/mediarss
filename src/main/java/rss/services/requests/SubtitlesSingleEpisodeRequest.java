package rss.services.requests;

import rss.entities.Show;
import rss.entities.Torrent;
import rss.services.subtitles.SubtitleLanguage;

import java.util.List;

/**
 * User: dikmanm
 * Date: 13/05/13 22:02
 */
public class SubtitlesSingleEpisodeRequest extends SubtitlesEpisodeRequest {

	private int episode;

	public SubtitlesSingleEpisodeRequest(Torrent torrent, Show show, int season, int episode, List<SubtitleLanguage> languages) {
		super(torrent, show, season, languages);
		this.episode = episode;
	}

	public int getEpisode() {
		return episode;
	}
}
