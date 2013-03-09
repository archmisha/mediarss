package rss.entities;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 24/12/12
 * Time: 23:26
 */
@Entity
@Table(name = "show")
@NamedQueries({
		// searching with lower case, so when user input "suits" it will also find "Suits"
		@NamedQuery(name = "Show.findByName",
				query = "select b from Show as b where lower(b.name) = :name"),
		@NamedQuery(name = "Show.getNotEnded",
				query = "select b from Show as b where b.ended = false")
})
public class Show extends BaseEntity {
	private static final long serialVersionUID = -4408596786454177485L;

	@Column(name = "name", unique = true)
	private String name;

	@Column(name = "created")
	private Date created;

	@Column(name = "tvcom_url")
	private String tvComUrl;

	@Column(name = "ended")
	private boolean ended;


	@OneToMany(mappedBy = "show", targetEntity = Episode.class)
	private Set<Episode> episodes;

	@Column(name = "tvrage_id")
	private int tvRageId;

	public Show() {
		created = new Date();
		episodes = new HashSet<>();
	}

	public Show(String name) {
		this();
		this.name = name;
	}

	public boolean isEnded() {
		return ended;
	}

	public void setEnded(boolean ended) {
		this.ended = ended;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTvComUrl() {
		return tvComUrl;
	}

	public void setTvComUrl(String tvComUrl) {
		this.tvComUrl = tvComUrl;
	}

	public Set<Episode> getEpisodes() {
		return episodes;
	}

	@Override
	public String toString() {
		return name + " (id=" + id + ", tvrage_id=" + tvRageId + ")";
	}

	public void setTvRageId(int tvRageId) {
		this.tvRageId = tvRageId;
	}

	public int getTvRageId() {
		return tvRageId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Show show = (Show) o;

		if (!name.equals(show.name)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
