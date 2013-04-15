package rss.services.requests;

import rss.entities.MediaQuality;
import rss.entities.Show;
import rss.util.StringUtils;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 15:42
 */
public class DoubleEpisodeRequest extends EpisodeRequest {

	private static final long serialVersionUID = 6690542669869677922L;

	private SingleEpisodeRequest episode1;
	private SingleEpisodeRequest episode2;

	public DoubleEpisodeRequest(String title, Show show, MediaQuality quality, int season, SingleEpisodeRequest episode1, SingleEpisodeRequest episode2) {
		super(title, show, quality, season);
		this.episode1 = episode1;
		this.episode2 = episode2;
	}

	public String getSeasonEpisode() {
			return "s" + StringUtils.pad(getSeason(), 2) + "e" + StringUtils.pad(episode1.getEpisode(), 2)+ "e" + StringUtils.pad(episode2.getEpisode(), 2);
	}

	@Override
	public EpisodeRequest copy() {
		return new DoubleEpisodeRequest(getTitle(), getShow(), getQuality(), getSeason(), getEpisode1(), getEpisode2());
	}

	public SingleEpisodeRequest getEpisode1() {
		return episode1;
	}

	public void setEpisode1(SingleEpisodeRequest episode1) {
		this.episode1 = episode1;
	}

	public SingleEpisodeRequest getEpisode2() {
		return episode2;
	}

	public void setEpisode2(SingleEpisodeRequest episode2) {
		this.episode2 = episode2;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DoubleEpisodeRequest that = (DoubleEpisodeRequest) o;

		if (!getEpisode1().equals(that.getEpisode1())) return false;
		if (!getEpisode2().equals(that.getEpisode2())) return false;
		if (!getTitle().equalsIgnoreCase(that.getTitle()))
			return false; // important ignore case! - when come from search for example
		if (getSeason() != that.getSeason()) return false;
		if (getQuality() != that.getQuality()) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = getQuality().hashCode();
		result = 31 * result + getTitle().toLowerCase().hashCode(); // to match ignore case equals
		result = 31 * result + getSeason();
		result = 31 * result + getEpisode1().hashCode();
		result = 31 * result + getEpisode2().hashCode();
		return result;
	}
}
