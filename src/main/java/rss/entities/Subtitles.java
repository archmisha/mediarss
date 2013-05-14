package rss.entities;

import org.hibernate.annotations.Index;
import rss.services.searchers.Downloadable;
import rss.services.subtitles.SubtitleLanguage;

import javax.persistence.*;
import java.util.Date;

/**
 * User: dikmanm
 * Date: 25/01/13 00:08
 */
@Entity
@Table(name = "subtitles")
@org.hibernate.annotations.Table(appliesTo = "subtitles", indexes = {
		@Index(name = "subtitles_language_torrent_id_idx", columnNames = {"language", "torrent_id"})
})
@NamedQueries({
		@NamedQuery(name = "Subtitles.find",
				query = "select s from Subtitles as s " +
						"where s.torrent.id = :torrentId and s.language = :language"),
		@NamedQuery(name = "Subtitles.findByTorrent",
				query = "select s from Subtitles as s " +
						"where s.torrent.id = :torrentId"),
		@NamedQuery(name = "Subtitles.getSubtitlesLanguages",
				query = "select u.subtitles from User as u " +
						"where u.subtitles is not null"),
		@NamedQuery(name = "Subtitles.getSubtitlesLanguagesForTorrent",
				query = "select u.subtitles from User as u join u.shows as s join s.episodes as e join e.torrentIds as tid " +
						"where u.subtitles is not null and :torrentId = tid")
})
public class Subtitles extends Media implements Downloadable {
	private static final long serialVersionUID = 5929747050786576285L;

	@Column(name = "language")
	private SubtitleLanguage language;

	@ManyToOne(targetEntity = Torrent.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "torrent_id")
	private Torrent torrent;

	@Column(name = "release_name")
	private String releaseName;

	@Column(name = "data")
	@Lob
	private byte[] data;

	@Column(name = "external_id")
	private String externalId;

	@Column(name = "file_name")
	private String fileName;

	@Column(name = "date_uploaded")
	@Index(name = "date_uploaded_idx")
	private Date dateUploaded;

	public SubtitleLanguage getLanguage() {
		return language;
	}

	public void setLanguage(SubtitleLanguage language) {
		this.language = language;
	}

	public Torrent getTorrent() {
		return torrent;
	}

	public void setTorrent(Torrent torrent) {
		this.torrent = torrent;
	}

	public Date getDateUploaded() {
		return dateUploaded;
	}

	public String getReleaseName() {
		return releaseName;
	}

	public void setReleaseName(String releaseName) {
		this.releaseName = releaseName;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public String getName() {
		return fileName;
	}
}
