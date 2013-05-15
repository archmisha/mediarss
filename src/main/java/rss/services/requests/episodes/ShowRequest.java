package rss.services.requests.episodes;

import rss.entities.MediaQuality;
import rss.entities.Show;
import rss.services.requests.MediaRequest;
import rss.services.requests.episodes.EpisodeRequest;
import rss.services.searchers.MediaRequestVisitor;

/**
 * User: dikmanm
 * Date: 15/04/13 10:18
 */
public abstract class ShowRequest extends MediaRequest {

	private MediaQuality quality;
	private Show show;

	public ShowRequest(String title, Show show, MediaQuality quality) {
		super(title, 1);
		this.quality = quality;
		this.show = show;
	}

	public void setQuality(MediaQuality quality) {
		this.quality = quality;
	}

	public MediaQuality getQuality() {
		return quality;
	}

	public void setShow(Show show) {
		this.show = show;
	}

	public Show getShow() {
		return show;
	}

	public abstract EpisodeRequest copy();


	@Override
	public <S, T> T visit(MediaRequestVisitor<S, T> visitor, S config) {
		throw new UnsupportedOperationException();
	}
}
