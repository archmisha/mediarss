package rss.entities;

import org.hibernate.annotations.Index;

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
		@NamedQuery(name = "Show.findByTvRageId",
				query = "select b from Show as b where b.tvRageId = :tvRageId"),
//		@NamedQuery(name = "Show.autoCompleteShowNames",
//				query = "select b from Show as b where lower(b.name) like :term"),
//		@NamedQuery(name = "Show.getNotEnded",
//				query = "select b from Show as b where b.ended = false"),
		@NamedQuery(name = "Show.findCachedShows",
				query = "select new rss.services.shows.CachedShow(b.id, b.name, b.ended) from Show as b")
})
public class Show extends BaseEntity {

	private static final long serialVersionUID = -4408596786454177485L;

	@Column(name = "name", unique = true)
	@Index(name ="show_name_idx")
	private String name;

	@Column(name = "tvcom_url")
	private String tvComUrl;

	@Column(name = "ended")
	private boolean ended;

	@Column(name = "schedule_download_date")
	private Date scheduleDownloadDate;

	@OneToMany(mappedBy = "show", targetEntity = Episode.class)
	private Set<Episode> episodes;

	@Column(name = "tvrage_id", unique = true)
	private int tvRageId;

	//, unique = true
	@Column(name = "subcenter_url")
	private String subCenterUrl;

	@Column(name = "subcenter_url_scan_date")
	private Date subCenterUrlScanDate;

	public Show() {
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

	public Date getScheduleDownloadDate() {
		return scheduleDownloadDate;
	}

	public void setScheduleDownloadDate(Date scheduleDownloadDate) {
		this.scheduleDownloadDate = scheduleDownloadDate;
	}

	public String getSubCenterUrl() {
		return subCenterUrl;
	}

	public void setSubCenterUrl(String subCenterUrl) {
		this.subCenterUrl = subCenterUrl;
	}

	public Date getSubCenterUrlScanDate() {
		return subCenterUrlScanDate;
	}

	public void setSubCenterUrlScanDate(Date subCenterUrlScanDate) {
		this.subCenterUrlScanDate = subCenterUrlScanDate;
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
