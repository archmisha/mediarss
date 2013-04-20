package rss.controllers.vo;

import rss.entities.Torrent;
import rss.entities.UserTorrent;

import java.util.Date;

/**
 * User: Michael Dikman
 * Date: 09/12/12
 * Time: 23:52
 */
public class UserMovieStatus {

	private String title;
	private long torrentId;
	private DownloadStatus downloadStatus;
	private boolean viewed;
	private Date downloadDate;
	private Date uploadedDate;
	private Date scheduledDate;
	private long movieId;

	public UserMovieStatus(DownloadStatus downloadStatus) {
		this.downloadStatus = downloadStatus;
	}

	public String getTitle() {
		return title;
	}

	public long getTorrentId() {
		return torrentId;
	}

	public UserMovieStatus withTitle(String title) {
		this.title = title;
		return this;
	}

	public UserMovieStatus withUploadedDate(Date uploadedDate) {
		this.uploadedDate = uploadedDate;
		return this;
	}

	public UserMovieStatus withTorrentId(long torrentId) {
		this.torrentId = torrentId;
		return this;
	}

	public Date getDownloadDate() {
		return downloadDate;
	}

	public UserMovieStatus withDownloadDate(Date downloadDate) {
		this.downloadDate = downloadDate;
		return this;
	}

	public UserMovieStatus withScheduledOn(Date scheduledOn) {
		this.scheduledDate = scheduledOn;
		return this;
	}

	public long getMovieId() {
		return movieId;
	}

	public UserMovieStatus withMovieId(long movieId) {
		this.movieId = movieId;
		return this;
	}

	public DownloadStatus getDownloadStatus() {
		return downloadStatus;
	}

	public boolean isViewed() {
		return viewed;
	}

	public void setDownloadStatus(DownloadStatus downloadStatus) {
		this.downloadStatus = downloadStatus;
	}

	public static UserMovieStatus fromUserTorrent(UserTorrent userTorrent) {
		Torrent torrent = userTorrent.getTorrent();

		DownloadStatus downloadStatus;
		if (userTorrent.getDownloadDate() != null) {
			downloadStatus = DownloadStatus.DOWNLOADED;
		} else {
			downloadStatus = DownloadStatus.SCHEDULED;
		}

		return new UserMovieStatus(downloadStatus)
				.withDownloadDate(userTorrent.getDownloadDate())
				.withTitle(torrent.getTitle())
				.withTorrentId(torrent.getId())
				.withUploadedDate(torrent.getDateUploaded())
				.withScheduledOn(userTorrent.getAdded());
	}

	public UserMovieStatus withViewed(boolean viewed) {
		this.viewed = viewed;
		return this;
	}

	public Date getUploadedDate() {
		return uploadedDate;
	}

	public Date getScheduledDate() {
		return scheduledDate;
	}
}
