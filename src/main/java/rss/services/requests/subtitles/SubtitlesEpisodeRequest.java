package rss.services.requests.subtitles;

import rss.entities.Show;
import rss.entities.Torrent;
import rss.services.subtitles.SubtitleLanguage;

import java.util.Date;
import java.util.List;

/**
 * User: dikmanm
 * Date: 11/05/13 15:11
 */
public abstract class SubtitlesEpisodeRequest extends SubtitlesRequest {

	private Show show;
	private int season;
	protected Date airDate;

	public SubtitlesEpisodeRequest(Torrent torrent, Show show, int season, List<SubtitleLanguage> languages) {
		super(torrent, show.getName(), languages);
		this.show = show;
		this.season = season;
	}

	public int getSeason() {
		return season;
	}

	public Show getShow() {
		return show;
	}

	public Date getAirDate() {
		return airDate;
	}
}
