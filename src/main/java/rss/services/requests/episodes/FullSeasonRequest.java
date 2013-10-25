package rss.services.requests.episodes;

import rss.entities.MediaQuality;
import rss.entities.Show;
import rss.services.searchers.MediaRequestVisitor;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 15:42
 */
public class FullSeasonRequest extends EpisodeRequest {

	public FullSeasonRequest(Long userId, String title, Show show, MediaQuality quality, int season) {
		super(userId, title, show, quality, season);
	}

	public String getSeasonEpisode() {
		return "season " + getSeason();
	}

	@Override
	public EpisodeRequest copy() {
		return new FullSeasonRequest(getUserId(), getTitle(), getShow(), getQuality(), getSeason());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FullSeasonRequest that = (FullSeasonRequest) o;

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
		return result;
	}

	@Override
	public <S, T> T visit(MediaRequestVisitor<S, T> visitor, S config) {
		return visitor.visit(this, config);
	}
}
