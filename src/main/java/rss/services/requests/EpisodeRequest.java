package rss.services.requests;

import rss.entities.MediaQuality;
import rss.entities.Show;
import rss.services.searchers.MediaRequestVisitor;
import rss.services.shows.ShowServiceImpl;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 15:42
 */
public abstract class EpisodeRequest extends ShowRequest {

	private int season;

	public EpisodeRequest(String title, Show show, MediaQuality quality, int season) {
		super(title, show, quality);
		this.season = season;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append(getTitle());
		sb.append(" ").append(getSeasonEpisode());
		if (getQuality() != null && getQuality() != MediaQuality.NORMAL) {
			sb.append(" ").append(getQuality());
		}
		return sb.toString();
	}

	@Override
	public String toQueryString() {
		// must use a different normalization method that the one used in comapring shows
		// because when searching we don't want to remove 'and', '&' and others
		// brothers and sisters vs brothers sisters give different results
		StringBuilder sb = new StringBuilder().append(ShowServiceImpl.normalizeForQueryString(getTitle()));
		sb.append(" ").append(getSeasonEpisode());
//		if (getQuality() != MediaQuality.NORMAL) {
//			sb.append(" ").append(getQuality());
//		}
		return sb.toString();
	}

	public abstract String getSeasonEpisode();

	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	@Override
	public <S, T> T visit(MediaRequestVisitor<S, T> visitor, S config) {
		return visitor.visit(this, config);
	}
}
