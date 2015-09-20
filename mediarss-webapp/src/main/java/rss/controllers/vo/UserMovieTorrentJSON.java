package rss.controllers.vo;

import rss.entities.UserMovieTorrent;
import rss.torrents.Torrent;
import rss.torrents.UserTorrentJSON;

/**
 * User: Michael Dikman
 * Date: 09/12/12
 * Time: 23:52
 */
public class UserMovieTorrentJSON extends UserTorrentJSON {

    public static UserMovieTorrentJSON fromUserTorrent(UserMovieTorrent userTorrent) {
        return populate(new UserMovieTorrentJSON(), userTorrent);
    }

    public static UserMovieTorrentJSON fromTorrent(Torrent torrent) {
        return populate(new UserMovieTorrentJSON(), torrent);
    }
}
