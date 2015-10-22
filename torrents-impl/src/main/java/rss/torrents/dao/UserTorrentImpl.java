package rss.torrents.dao;

import rss.ems.entities.BaseEntity;
import rss.torrents.Torrent;
import rss.torrents.UserTorrent;
import rss.user.User;
import rss.user.dao.UserImpl;

import javax.persistence.*;
import java.util.Date;

/**
 * User: Michael Dikman
 * Date: 08/12/12
 * Time: 14:26
 */
@MappedSuperclass
public abstract class UserTorrentImpl extends BaseEntity implements UserTorrent {

	private static final long serialVersionUID = 3494206029265679353L;

	@ManyToOne(targetEntity = UserImpl.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "added")
	private Date added;

	@Column(name = "download_date")
	private Date downloadDate;

	@ManyToOne(targetEntity = TorrentImpl.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "torrent_id")
	private Torrent torrent;

	public UserTorrentImpl() {
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

	public Date getDownloadDate() {
		return downloadDate;
	}

	public void setDownloadDate(Date downloadDate) {
		this.downloadDate = downloadDate;
	}
}
