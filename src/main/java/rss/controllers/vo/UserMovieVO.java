package rss.controllers.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: Michael Dikman
 * Date: 06/12/12
 * Time: 00:12
 */
public class UserMovieVO {

	private long id;
	private String title;
	private String imdbUrl;
	private List<UserMovieStatus> torrents;
	private Date latestUploadDate;
	private DownloadStatus downloadStatus;
	private boolean viewed;
	private Date scheduledDate;

	public UserMovieVO() {
		viewed = false;
		torrents = new ArrayList<>();
		downloadStatus = DownloadStatus.NONE;
	}

	public boolean isViewed() {
		return viewed;
	}

	public void setViewed(boolean isViewed) {
		this.viewed = isViewed;
	}

	public void addTorrentDownloadStatus(UserMovieStatus userMovieStatus) {
		torrents.add(userMovieStatus);

		// if any torrent got downloaded status use it, otherwise the next in line is scheduled and if nothing else the default is none.
		if (userMovieStatus.getDownloadStatus() == DownloadStatus.DOWNLOADED) {
			downloadStatus = DownloadStatus.DOWNLOADED;
		} else if (userMovieStatus.getDownloadStatus() == DownloadStatus.SCHEDULED && downloadStatus != DownloadStatus.DOWNLOADED) {
			downloadStatus = DownloadStatus.SCHEDULED;
		}
	}

	public long getId() {
		return id;
	}

	public UserMovieVO withId(long id) {
		this.id = id;
		return this;
	}

	public UserMovieVO withTitle(String title) {
		this.title = title;
		return this;
	}

	public UserMovieVO withImdbUrl(String imdbUrl) {
		this.imdbUrl = imdbUrl;
		return this;
	}

	public void setLatestUploadDate(Date latestUploadDate) {
		this.latestUploadDate = latestUploadDate;
	}

	public Date getLatestUploadDate() {
		return latestUploadDate;
	}

	public DownloadStatus getDownloadStatus() {
		return downloadStatus;
	}

	public String getTitle() {
		return title;
	}

	public String getImdbUrl() {
		return imdbUrl;
	}

	public List<UserMovieStatus> getTorrents() {
		return torrents;
	}

	public void setDownloadStatus(DownloadStatus downloadStatus) {
		this.downloadStatus = downloadStatus;
	}

	public UserMovieVO withScheduledOn(Date scheduledOn) {
		this.scheduledDate = scheduledOn;
		return this;
	}

	public Date getScheduledDate() {
		return scheduledDate;
	}
}
