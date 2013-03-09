package rss.controllers.vo;

/**
 * User: dikmanm
 * Date: 26/12/12 20:59
 */
public class UserTorrentVO {

	private String title;
	private long torrentId;
	private boolean downloaded;

	public UserTorrentVO() {
	}

	public boolean isDownloaded() {
		return downloaded;
	}

	public String getTitle() {
		return title;
	}

	public long getTorrentId() {
		return torrentId;
	}

	public UserTorrentVO withTitle(String title) {
		this.title = title;
		return this;
	}

	public UserTorrentVO withTorrentId(long torrentId) {
		this.torrentId = torrentId;
		return this;
	}

	public UserTorrentVO withDownloaded(boolean downloaded) {
		this.downloaded = downloaded;
		return this;
	}
}
