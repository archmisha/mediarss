package rss.entities;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 14:26
 */
@Entity
@Table(name = "episode")
@org.hibernate.annotations.Table(appliesTo = "episode", indexes = {
		@Index(name = "ep_showid_season_episode_idx", columnNames = {"show_id", "season", "episode"}),
		@Index(name = "ep_airdate_showId_idx", columnNames = {"air_date", "show_id"})
})
@NamedQueries({
		@NamedQuery(name = "Episode.getSubtitlesLanguages",
				query = "select u.subtitles from User as u join u.shows as s join s.episodes as e " +
						"where u.subtitles is not null and e.id = :episodeId"),
		@NamedQuery(name = "Episode.findByTorrent",
				query = "select e from Episode as e join e.torrentIds as tid " +
						"where :torrentId = tid")
})
public class Episode extends Media {

	private static final long serialVersionUID = 4424414890903298183L;

	@Column(name = "season")
	private int season;

	@Column(name = "episode")
	private int episode;

	// eager cuz need it later for comparator stuff of show name
	@ManyToOne(targetEntity = Show.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "show_id")
	@Index(name = "episode_showid_idx")
	private Show show;

	@Column(name = "air_date")
	@Index(name = "episode_airdate_idx")
	private Date airDate;

	@Column(name = "scan_date")
	private Date scanDate;

	@SuppressWarnings("UnusedDeclaration")
	public Episode() {
	}

	@Override
	public String getName() {
		return show.getName();
	}

	public Episode(int season, int episode) {
		this.season = season;
		this.episode = episode;
	}

	public int getSeason() {
		return season;
	}

	public int getEpisode() {
		return episode;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	public void setEpisode(int episode) {
		this.episode = episode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append(getName()).append(" ").append(getSeasonEpisode());
		if (airDate != null) {
			sb.append(" air date ").append(new SimpleDateFormat("yyyy/MM/dd").format(airDate));
		}
		return sb.toString();
	}

	public String getSeasonEpisode() {
		StringBuilder sb = new StringBuilder().append("s").append(StringUtils.leftPad(String.valueOf(season), 2, '0'));
		if (episode > 0) {
			sb.append("e").append(StringUtils.leftPad(String.valueOf(episode), 2, '0'));
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Episode that = (Episode) o;

		if (episode != that.episode) return false;
		if (season != that.season) return false;
		if (!getName().equals(that.getName())) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = getName().hashCode();
		result = 31 * result + season;
		result = 31 * result + episode;
		return result;
	}

	public void setShow(Show show) {
		this.show = show;
	}

	public Date getAirDate() {
		return airDate;
	}

	public void setAirDate(Date airDate) {
		this.airDate = airDate;
	}

	public Date getScanDate() {
		return scanDate;
	}

	public void setScanDate(Date scanDate) {
		this.scanDate = scanDate;
	}

	public Show getShow() {
		return show;
	}

	public boolean isUnAired() {
		return getAirDate() != null && getAirDate().after(new Date());
	}
}
