package rss.services.requests;

import rss.entities.Show;
import rss.entities.Torrent;

/**
 * User: dikmanm
 * Date: 13/05/13 22:02
 */
public class SubtitlesSingleEpisodeRequest extends SubtitlesEpisodeRequest {

	private int episode;

	public SubtitlesSingleEpisodeRequest(Torrent torrent, Show show, int season, int episode) {
		super(torrent, show, season);
		this.episode = episode;
	}

	public int getEpisode() {
		return episode;
	}
}
