package rss.subtitles.dao;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;

/**
 * User: dikmanm
 * Date: 18/05/13 11:59
 */
@javax.persistence.Entity
@DiscriminatorValue(value = "single_episode")
public class SingleEpisodeSubtitles extends EpisodeSubtitles {
	private static final long serialVersionUID = 5160599347471452815L;

	@Column(name = "episode")
	private int episode;

	// for hibernate
	@SuppressWarnings("UnusedDeclaration")
	private SingleEpisodeSubtitles() {
	}

	public SingleEpisodeSubtitles(int season, int episode) {
		super(season);
		this.episode = episode;
	}

	public int getEpisode() {
		return episode;
	}

	public void setEpisode(int episode) {
		this.episode = episode;
	}
}
