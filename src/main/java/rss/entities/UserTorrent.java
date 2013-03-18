package rss.entities;

import javax.persistence.*;
import java.util.Date;

/**
 * User: Michael Dikman
 * Date: 08/12/12
 * Time: 14:26
 */
@MappedSuperclass
public abstract class UserTorrent extends BaseEntity {

	private static final long serialVersionUID = 3494206029265679353L;

	@ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "added")
	private Date added;

	@Column(name = "download_date")
	private Date downloadDate;

	@ManyToOne(targetEntity = Torrent.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "torrent_id")
	private Torrent torrent;

	public UserTorrent() {
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getAdded() {
		return added;
	}

	public void setAdded(Date added) {
		this.added = added;
	}

	public Torrent getTorrent() {
		return torrent;
	}

	public void setTorrent(Torrent torrent) {
		this.torrent = torrent;
	}

//	public void setIgnored(boolean ignored) {
//		this.ignored = ignored;
//	}

//	public boolean isIgnored() {
//		return ignored;
//	}

	public Date getDownloadDate() {
		return downloadDate;
	}

	public void setDownloadDate(Date downloadDate) {
		this.downloadDate = downloadDate;
	}
}
