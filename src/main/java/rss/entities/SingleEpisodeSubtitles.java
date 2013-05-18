package rss.entities;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * User: dikmanm
 * Date: 18/05/13 11:59
 */
@Entity
@Table(name = "subtitles")
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
