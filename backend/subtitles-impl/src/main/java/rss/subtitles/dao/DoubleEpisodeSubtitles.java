package rss.subtitles.dao;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;

/**
 * User: dikmanm
 * Date: 18/05/13 11:59
 */
@javax.persistence.Entity
@DiscriminatorValue(value = "double_episode")
public class DoubleEpisodeSubtitles extends EpisodeSubtitles {
	private static final long serialVersionUID = 5160599347471452815L;

	// reuse the same column with single episode
	@Column(name = "episode")
	private int episode;

	@Column(name = "episode2")
	private int episode2;

	// for hibernate
	@SuppressWarnings("UnusedDeclaration")
	private DoubleEpisodeSubtitles() {
	}

	public DoubleEpisodeSubtitles(int season, int episode1, int episode2) {
		super(season);
		this.episode = episode1;
		this.episode2 = episode2;
	}

	public int getEpisode() {
		return episode;
	}

	public void setEpisode(int episode) {
		this.episode = episode;
	}

	public int getEpisode2() {
		return episode2;
	}

	public void setEpisode2(int episode2) {
		this.episode2 = episode2;
	}
}
