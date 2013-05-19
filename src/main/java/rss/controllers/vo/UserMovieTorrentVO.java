package rss.controllers.vo;

import rss.entities.Torrent;
import rss.entities.UserMovieTorrent;
import rss.entities.UserTorrent;

import java.util.Date;

/**
 * User: Michael Dikman
 * Date: 09/12/12
 * Time: 23:52
 */
public class UserMovieTorrentVO {

	private String title;
	private long torrentId;
	private DownloadStatus downloadStatus;
	private boolean viewed;
	private Date downloadDate;
	private Date uploadedDate;
	private Date scheduledDate;
	private long movieId;
	private int size;

	public UserMovieTorrentVO(DownloadStatus downloadStatus) {
		this.downloadStatus = downloadStatus;
	}

	public String getTitle() {
		return title;
	}

	public long getTorrentId() {
		return torrentId;
	}

	public UserMovieTorrentVO withTitle(String title) {
		this.title = title;
		return this;
	}

	public UserMovieTorrentVO withUploadedDate(Date uploadedDate) {
		this.uploadedDate = uploadedDate;
		return this;
	}

	public UserMovieTorrentVO withTorrentId(long torrentId) {
		this.torrentId = torrentId;
		return this;
	}

	public Date getDownloadDate() {
		return downloadDate;
	}

	public UserMovieTorrentVO withSize(int size) {
		this.size = size;
		return this;
	}

	public UserMovieTorrentVO withDownloadDate(Date downloadDate) {
		this.downloadDate = downloadDate;
		return this;
	}

	public UserMovieTorrentVO withScheduledOn(Date scheduledOn) {
		this.scheduledDate = scheduledOn;
		return this;
	}

	public long getMovieId() {
		return movieId;
	}

	public UserMovieTorrentVO withMovieId(long movieId) {
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

	public static UserMovieTorrentVO fromUserTorrent(UserMovieTorrent userTorrent) {
		Torrent torrent = userTorrent.getTorrent();

		DownloadStatus downloadStatus;
		if (userTorrent.getDownloadDate() != null) {
			downloadStatus = DownloadStatus.DOWNLOADED;
		} else {
			downloadStatus = DownloadStatus.SCHEDULED;
		}

		return new UserMovieTorrentVO(downloadStatus)
				.withDownloadDate(userTorrent.getDownloadDate())
				.withTitle(torrent.getTitle())
				.withTorrentId(torrent.getId())
				.withUploadedDate(torrent.getDateUploaded())
				.withSize(torrent.getSize())
				.withScheduledOn(userTorrent.getAdded())
				.withMovieId(userTorrent.getUserMovie().getMovie().getId());
	}

	public static UserMovieTorrentVO fromTorrent(Torrent torrent, long movieId) {
		return new UserMovieTorrentVO(DownloadStatus.NONE)
				.withTitle(torrent.getTitle())
				.withTorrentId(torrent.getId())
				.withUploadedDate(torrent.getDateUploaded())
				.withScheduledOn(null)
				.withSize(torrent.getSize())
				.withMovieId(movieId);
	}

	public UserMovieTorrentVO withViewed(boolean viewed) {
		this.viewed = viewed;
		return this;
	}

	public Date getUploadedDate() {
		return uploadedDate;
	}

	public Date getScheduledDate() {
		return scheduledDate;
	}

	public int getSize() {
		return size;
	}
}
