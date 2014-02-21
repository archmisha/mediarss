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
	private List<UserMovieTorrentVO> torrents;
	//	private Date latestUploadDate;
	private DownloadStatus downloadStatus;
	private boolean viewed;
	private Date scheduledDate;
	private Date added;
	private Date releaseDate;

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

	public void addUserMovieTorrent(UserMovieTorrentVO userMovieTorrentVO/*, Date torrentUploadDate*/) {
		torrents.add(userMovieTorrentVO);

		// if any torrent got downloaded status use it, otherwise the next in line is scheduled and if nothing else the default is none.
		if (userMovieTorrentVO.getDownloadStatus() == DownloadStatus.DOWNLOADED) {
			downloadStatus = DownloadStatus.DOWNLOADED;
		} else if (userMovieTorrentVO.getDownloadStatus() == DownloadStatus.SCHEDULED && downloadStatus != DownloadStatus.DOWNLOADED) {
			downloadStatus = DownloadStatus.SCHEDULED;
		}

//		if (latestUploadDate == null || latestUploadDate.before(torrentUploadDate)) {
//			latestUploadDate = torrentUploadDate;
//		}
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

	public DownloadStatus getDownloadStatus() {
		return downloadStatus;
	}

	public String getTitle() {
		return title;
	}

	public List<UserMovieTorrentVO> getTorrents() {
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

	public UserMovieVO withAdded(Date added) {
		this.added = added;
		return this;
	}

	public Date getAdded() {
		return added;
	}

	public UserMovieVO withReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
		return this;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}
}
