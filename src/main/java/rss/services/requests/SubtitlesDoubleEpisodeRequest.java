package rss.services.requests;

import rss.entities.Show;
import rss.entities.Torrent;

/**
 * User: dikmanm
 * Date: 11/05/13 20:14
 */
public class SubtitlesDoubleEpisodeRequest extends SubtitlesEpisodeRequest {

	private int episode1;
	private int episode2;

	public SubtitlesDoubleEpisodeRequest(Torrent torrent, Show show, int season, int episode1, int episode2) {
		super(torrent, show, season);
		this.episode1 = episode1;
		this.episode2 = episode2;
	}

	public int getEpisode1() {
		return episode1;
	}

	public int getEpisode2() {
		return episode2;
	}
}
