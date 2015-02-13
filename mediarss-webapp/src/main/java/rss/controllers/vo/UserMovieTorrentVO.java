package rss.controllers.vo;

import rss.entities.Torrent;
import rss.entities.UserMovieTorrent;

/**
 * User: Michael Dikman
 * Date: 09/12/12
 * Time: 23:52
 */
public class UserMovieTorrentVO extends UserTorrentVO {

	public static UserMovieTorrentVO fromUserTorrent(UserMovieTorrent userTorrent) {
		return populate(new UserMovieTorrentVO(), userTorrent);
	}

	public static UserMovieTorrentVO fromTorrent(Torrent torrent) {
		return populate(new UserMovieTorrentVO(), torrent);
	}
}
