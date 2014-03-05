package rss.controllers.vo;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: Michael Dikman
 * Date: 06/12/12
 * Time: 00:12
 */
@JsonIgnoreProperties({"added", "releaseDate"})
public class UserMovieVO {
	private long id;
	private String title;
	private List<UserMovieTorrentVO> viewedTorrents;
	private List<UserMovieTorrentVO> notViewedTorrents;
	private int viewedTorrentsCount;
	private int notViewedTorrentsCount;
	private DownloadStatus downloadStatus;
	private Date scheduledDate;
	private Date added;
	private Date releaseDate;

	public UserMovieVO() {
		viewedTorrents = new ArrayList<>();
		notViewedTorrents = new ArrayList<>();
		downloadStatus = DownloadStatus.NONE;
		viewedTorrentsCount = 0;
		notViewedTorrentsCount = 0;
	}

	public void addUserMovieTorrent(UserMovieTorrentVO userMovieTorrentVO, boolean isViewed) {
		// make added/scheduled torrents to be on top in the sort
		if (isViewed && userMovieTorrentVO.getScheduledDate() == null) {
			viewedTorrents.add(userMovieTorrentVO);
			viewedTorrentsCount++;
		} else {
			notViewedTorrents.add(userMovieTorrentVO);
			notViewedTorrentsCount++;
		}

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

	public List<UserMovieTorrentVO> getViewedTorrents() {
		return viewedTorrents;
	}

	public List<UserMovieTorrentVO> getNotViewedTorrents() {
		return notViewedTorrents;
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

	public int getViewedTorrentsCount() {
		return viewedTorrentsCount;
	}

	public int getNotViewedTorrentsCount() {
		return notViewedTorrentsCount;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setViewedTorrentsCount(int viewedTorrentsCount) {
		this.viewedTorrentsCount = viewedTorrentsCount;
	}

	public void setNotViewedTorrentsCount(int notViewedTorrentsCount) {
		this.notViewedTorrentsCount = notViewedTorrentsCount;
	}

	public void setScheduledDate(Date scheduledDate) {
		this.scheduledDate = scheduledDate;
	}

	public void setAdded(Date added) {
		this.added = added;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}
}
