package rss.services.requests;

import rss.entities.Show;
import rss.entities.Torrent;

/**
 * User: dikmanm
 * Date: 11/05/13 15:11
 */
public abstract class SubtitlesEpisodeRequest extends SubtitlesRequest {

	private Show show;
	private int season;

	public SubtitlesEpisodeRequest(Torrent torrent, Show show, int season) {
		super(torrent, show.getName());
		this.show = show;
		this.season = season;
	}

	public int getSeason() {
		return season;
	}

	public Show getShow() {
		return show;
	}
}
