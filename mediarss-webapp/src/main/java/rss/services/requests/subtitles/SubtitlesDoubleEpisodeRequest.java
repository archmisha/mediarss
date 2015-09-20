package rss.services.requests.subtitles;

import org.apache.commons.lang3.StringUtils;
import rss.entities.Show;
import rss.services.subtitles.SubtitleLanguage;
import rss.torrents.Torrent;

import java.util.Date;
import java.util.List;

/**
 * User: dikmanm
 * Date: 11/05/13 20:14
 */
public class SubtitlesDoubleEpisodeRequest extends SubtitlesEpisodeRequest {

	private int episode1;
	private int episode2;

	public SubtitlesDoubleEpisodeRequest(Torrent torrent, Show show, int season, int episode1, int episode2, List<SubtitleLanguage> languages, Date episode1AirDate, Date episode2AirDate) {
		super(torrent, show, season, languages);
		this.episode1 = episode1;
		this.episode2 = episode2;

		if (episode1AirDate == null) {
			this.airDate = episode2AirDate;
		} else if (episode2AirDate == null) {
			this.airDate = episode1AirDate;
		} else {
			this.airDate = episode1AirDate.before(episode2AirDate) ? episode1AirDate : episode2AirDate;
		}
	}

	public int getEpisode1() {
		return episode1;
	}

	public int getEpisode2() {
		return episode2;
	}

	public String getSeasonEpisode() {
		return "s" + StringUtils.leftPad(String.valueOf(getSeason()), 2, '0') +
			   "e" + StringUtils.leftPad(String.valueOf(episode1), 2, '0') +
			   "e" + StringUtils.leftPad(String.valueOf(episode2), 2, '0');
	}
}
