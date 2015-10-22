package rss.torrents;

import org.springframework.stereotype.Component;

/**
 * User: dikmanm
 * Date: 22/08/2015 18:53
 */
@Component
public class TorrentsConverter {

    public UserTorrentJSON fromUserTorrent(UserTorrent userTorrent) {
        return populate(new UserTorrentJSON(), userTorrent);
    }

    public <T extends UserTorrentJSON> T populate(T userTorrentVO, UserTorrent userTorrent) {
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

    public UserTorrentJSON fromTorrent(Torrent torrent) {
        return populate(new UserTorrentJSON(), torrent);
    }

    public <T extends UserTorrentJSON> T populate(T userTorrentVO, Torrent torrent) {
        userTorrentVO.withDownloadStatus(DownloadStatus.NONE)
                .withTitle(torrent.getTitle())
                .withTorrentId(torrent.getId())
                .withUploadedDate(torrent.getDateUploaded())
                .withScheduledOn(null)
                .withSize(torrent.getSize());
        return userTorrentVO;
    }
}
