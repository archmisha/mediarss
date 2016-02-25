package rss.torrents.requests.subtitles;

import rss.torrents.Show;
import rss.torrents.Torrent;
import rss.user.subtitles.SubtitleLanguage;

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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append(getName()).append(" ").append(getSeasonEpisode()).append(" ");
		for (SubtitleLanguage language : getLanguages()) {
			sb.append(language.name()).append(", ");
		}
		sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	protected abstract String getSeasonEpisode();
}
