package rss.services.requests;

import rss.entities.Show;
import rss.entities.Torrent;
import rss.services.subtitles.SubtitleLanguage;

import java.util.Date;
import java.util.List;

/**
 * User: dikmanm
 * Date: 11/05/13 20:14
 */
public class SubtitlesDoubleEpisodeRequest extends SubtitlesEpisodeRequest {

	private int episode1;
	private int episode2;
	private Date episode1AirDate;
	private Date episode2AirDate;

	public SubtitlesDoubleEpisodeRequest(Torrent torrent, Show show, int season, int episode1, int episode2, List<SubtitleLanguage> languages, Date episode1AirDate, Date episode2AirDate) {
		super(torrent, show, season, languages);
		this.episode1 = episode1;
		this.episode2 = episode2;
		this.episode1AirDate = episode1AirDate;
		this.episode2AirDate = episode2AirDate;
	}

	public int getEpisode1() {
		return episode1;
	}

	public int getEpisode2() {
		return episode2;
	}

	public Date getOldestAirDate() {
		if (episode1AirDate == null) {
			return episode2AirDate;
		}
		if (episode2AirDate == null) {
			return episode1AirDate;
		}
		return episode1AirDate.before(episode2AirDate) ? episode1AirDate : episode2AirDate;
	}
}
