package rss.services;

import rss.entities.MediaQuality;
import rss.entities.Show;
import rss.services.shows.ShowServiceImpl;
import rss.util.StringUtils;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 15:42
 */
public class EpisodeRequest extends MediaRequest {

	private static final long serialVersionUID = -3775136728830359029L;

	private MediaQuality quality;
	private int season;
	private int episode;
	private Show show;

	public EpisodeRequest(EpisodeRequest episodeRequest) {
		this(episodeRequest.getTitle(), episodeRequest.show, episodeRequest.quality, episodeRequest.season, episodeRequest.episode);
	}

	public EpisodeRequest(String title, Show show, MediaQuality quality, int season, int episode) {
		super(title);
		this.quality = quality;
		this.season = season;
		this.episode = episode;
		this.show = show;
	}

	public EpisodeRequest() {
		quality = MediaQuality.NORMAL;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append(getTitle());
		sb.append(" ").append(getSeasonEpisode());
		if (quality != null && quality != MediaQuality.NORMAL) {
			sb.append(" ").append(quality);
		}
		return sb.toString();
	}

	@Override
	public String toQueryString() {
		StringBuilder sb = new StringBuilder().append(ShowServiceImpl.normalize(getTitle()));
		sb.append(" ").append(getSeasonEpisode());
		if (quality != MediaQuality.NORMAL) {
			sb.append(" ").append(quality);
		}
		return sb.toString();
	}

	public String getSeasonEpisode() {
		if (episode > 0) {
			return "s" + StringUtils.pad(season, 2) + "e" + StringUtils.pad(episode, 2);
		} else {
			return "season " + season;
		}
	}

	public int getSeason() {
		return season;
	}

	public int getEpisode() {
		return episode;
	}

	public void setEpisode(int episode) {
		this.episode = episode;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	public void setQuality(MediaQuality quality) {
		this.quality = quality;
	}

	public MediaQuality getQuality() {
		return quality;
	}

	public Show getShow() {
		return show;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EpisodeRequest that = (EpisodeRequest) o;

		if (episode != that.episode) return false;
		if (!getTitle().equalsIgnoreCase(that.getTitle()))
			return false; // important ignore case! - when come from search for example
		if (season != that.season) return false;
		if (quality != that.quality) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = quality.hashCode();
		result = 31 * result + getTitle().toLowerCase().hashCode(); // to match ignore case equals
		result = 31 * result + season;
		result = 31 * result + episode;
		return result;
	}

	public void setShow(Show show) {
		this.show = show;
	}
}
