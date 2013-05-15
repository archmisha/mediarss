package rss.services.requests;

import rss.entities.Show;
import rss.entities.Torrent;
import rss.services.subtitles.SubtitleLanguage;

import java.util.Date;
import java.util.List;

/**
 * User: dikmanm
 * Date: 13/05/13 22:02
 */
public class SubtitlesSingleEpisodeRequest extends SubtitlesEpisodeRequest {

	private int episode;
	private Date airDate;

	public SubtitlesSingleEpisodeRequest(Torrent torrent, Show show, int season, int episode, List<SubtitleLanguage> languages, Date airDate) {
		super(torrent, show, season, languages);
		this.episode = episode;
		this.airDate = airDate;
	}

	public int getEpisode() {
		return episode;
	}

	public Date getAirDate() {
		return airDate;
	}
}
