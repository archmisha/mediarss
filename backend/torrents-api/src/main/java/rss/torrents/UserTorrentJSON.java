package rss.torrents;

import java.util.Date;

/**
 * User: dikmanm
 * Date: 26/12/12 20:59
 */
public class UserTorrentJSON {

    private String title;
    private long torrentId;
    private DownloadStatus downloadStatus;
    private Date downloadDate;
    private Date uploadedDate;
    private Date scheduledDate;
    private int size;

    public UserTorrentJSON withTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public UserTorrentJSON withTorrentId(long torrentId) {
        this.torrentId = torrentId;
        return this;
    }

    public long getTorrentId() {
        return torrentId;
    }

    public UserTorrentJSON withDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
        return this;
    }

    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

    public UserTorrentJSON withDownloadDate(Date downloadDate) {
        this.downloadDate = downloadDate;
        return this;
    }

    public Date getDownloadDate() {
        return downloadDate;
    }

    public UserTorrentJSON withUploadedDate(Date uploadedDate) {
        this.uploadedDate = uploadedDate;
        return this;
    }

    public Date getUploadedDate() {
        return uploadedDate;
    }

    public UserTorrentJSON withSize(int size) {
        this.size = size;
        return this;
    }

    public int getSize() {
        return size;
    }

    public UserTorrentJSON withScheduledOn(Date scheduledOn) {
        this.scheduledDate = scheduledOn;
        return this;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }
}
