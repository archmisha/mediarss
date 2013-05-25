package rss.controllers.vo;

import rss.entities.Torrent;
import rss.entities.UserTorrent;

import java.util.Date;

/**
 * User: dikmanm
 * Date: 26/12/12 20:59
 */
public class UserTorrentVO {

	private String title;
	private long torrentId;
	private DownloadStatus downloadStatus;
	private Date downloadDate;
	private Date uploadedDate;
	private Date scheduledDate;
	private int size;

	public UserTorrentVO() {
	}

	public static UserTorrentVO fromUserTorrent(UserTorrent userTorrent) {
		return populate(new UserTorrentVO(), userTorrent);
	}

	protected static <T extends UserTorrentVO> T populate(T userTorrentVO, UserTorrent userTorrent) {
		Torrent torrent = userTorrent.getTorrent();

		DownloadStatus downloadStatus;
		if (userTorrent.getDownloadDate() != null) {
			downloadStatus = DownloadStatus.DOWNLOADED;
		} else {
			downloadStatus = DownloadStatus.SCHEDULED;
		}

		userTorrentVO.withDownloadStatus(downloadStatus)
				.withDownloadDate(userTorrent.getDownloadDate())
				.withTitle(torrent.getTitle())
				.withTorrentId(torrent.getId())
				.withUploadedDate(torrent.getDateUploaded())
				.withSize(torrent.getSize())
				.withScheduledOn(userTorrent.getAdded());

		return userTorrentVO;
	}

	public static UserTorrentVO fromTorrent(Torrent torrent) {
		return populate(new UserTorrentVO(), torrent);
	}

	protected static <T extends UserTorrentVO> T populate(T userTorrentVO, Torrent torrent) {
		userTorrentVO.withDownloadStatus(DownloadStatus.NONE)
				.withTitle(torrent.getTitle())
				.withTorrentId(torrent.getId())
				.withUploadedDate(torrent.getDateUploaded())
				.withScheduledOn(null)
				.withSize(torrent.getSize());
		return userTorrentVO;
	}

	public UserTorrentVO withTitle(String title) {
		this.title = title;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public UserTorrentVO withTorrentId(long torrentId) {
		this.torrentId = torrentId;
		return this;
	}

	public long getTorrentId() {
		return torrentId;
	}

	public UserTorrentVO withDownloadStatus(DownloadStatus downloadStatus) {
		this.downloadStatus = downloadStatus;
		return this;
	}

	public DownloadStatus getDownloadStatus() {
		return downloadStatus;
	}

	public UserTorrentVO withDownloadDate(Date downloadDate) {
		this.downloadDate = downloadDate;
		return this;
	}

	public Date getDownloadDate() {
		return downloadDate;
	}

	public UserTorrentVO withUploadedDate(Date uploadedDate) {
		this.uploadedDate = uploadedDate;
		return this;
	}

	public Date getUploadedDate() {
		return uploadedDate;
	}

	public UserTorrentVO withSize(int size) {
		this.size = size;
		return this;
	}

	public int getSize() {
		return size;
	}

	public UserTorrentVO withScheduledOn(Date scheduledOn) {
		this.scheduledDate = scheduledOn;
		return this;
	}

	public Date getScheduledDate() {
		return scheduledDate;
	}
}
