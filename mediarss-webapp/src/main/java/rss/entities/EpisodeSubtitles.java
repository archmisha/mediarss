package rss.entities;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * User: dikmanm
 * Date: 18/05/13 11:59
 */
@MappedSuperclass
public abstract class EpisodeSubtitles extends Subtitles {
	private static final long serialVersionUID = 5160599347471452815L;

	@Column(name = "season")
	private int season;

	protected EpisodeSubtitles() {
	}

	protected EpisodeSubtitles(int season) {
		this.season = season;
	}

	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}
}
