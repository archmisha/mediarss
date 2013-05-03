package rss.entities;

import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.Date;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 14:36
 */
@Entity
@Table(name = "torrent")
@org.hibernate.annotations.Table(appliesTo = "torrent",
                                 indexes = {
                                         @Index(name = "torrent_date_uploaded_idx", columnNames = {"date_uploaded"})
                                 })
@NamedQueries({
		@NamedQuery(name = "Torrent.findByUrl",
				query = "select b from Torrent as b where b.url = :url")
})
public class Torrent extends BaseEntity implements Comparable<Torrent> {

    private static final long serialVersionUID = -8358871423354442763L;

    @Column(name = "title")
    private String title;

    @Column(name = "url", length = 4000, unique = true)
    private String url;

    @Column(name = "date_uploaded")
	@Index(name = "date_uploaded_idx")
    private Date dateUploaded;

    @Column(name = "quality")
    private MediaQuality quality;

	@Column(name = "hash")
	private String hash;

    // not stored in the database, because first it will become outdated and second it is only used while
    // searching for a specific episode on the internet
    @Transient
    private int seeders;

    @Transient
    private String sourcePageUrl;

	@Transient
	private String imdbId;

	// for hibernate
	@SuppressWarnings("UnusedDeclaration")
	private Torrent() {}

    public Torrent(String title, String url, Date dateUploaded, int seeders) {
        this(title, url, dateUploaded, seeders, null);
    }

    public Torrent(String title, String url, Date dateUploaded, int seeders, String sourcePageUrl) {
        this.title = title;
        this.url = url;
        this.dateUploaded = dateUploaded;
        this.seeders = seeders;
        this.sourcePageUrl = sourcePageUrl;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Torrent");
        sb.append("{title='").append(title).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", dateUploaded=").append(dateUploaded);
        sb.append('}');
        return sb.toString();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public Date getDateUploaded() {
        return dateUploaded;
    }

    public int getSeeders() {
        return seeders;
    }

    public String getSourcePageUrl() {
        return sourcePageUrl;
    }

    public void setSourcePageUrl(String sourcePageUrl) {
        this.sourcePageUrl = sourcePageUrl;
    }

    @Override
    public int compareTo(Torrent o) {
        return title.compareTo(o.title);
    }


    public MediaQuality getQuality() {
        return quality;
    }

    public void setQuality(MediaQuality quality) {
        this.quality = quality;
    }

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getHash() {
		return hash;
	}

	public String getImdbId() {
		return imdbId;
	}

	public void setImdbId(String imdbId) {
		this.imdbId = imdbId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Torrent torrent = (Torrent) o;

		if (hash != null ? !hash.equals(torrent.hash) : torrent.hash != null) return false;
		if (!title.equals(torrent.title)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = title.hashCode();
		result = 31 * result + (hash != null ? hash.hashCode() : 0);
		return result;
	}
}
