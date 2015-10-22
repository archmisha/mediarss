package rss.torrents.dao;

import rss.ems.dao.Dao;
import rss.torrents.UserTorrent;
import rss.user.User;

import java.util.Date;
import java.util.List;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface UserTorrentDao extends Dao<UserTorrent> {

    List<UserTorrent> findUserMoviesForUserFeed(Date dateUploaded, User user);

    List<UserTorrent> findEpisodesAddedSince(User user, Date dateUploaded);

    List<UserTorrent> findUserEpisodeTorrentByTorrentId(long torrentId);
}
