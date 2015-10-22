package rss.shows.dao;

import rss.shows.UserEpisodeTorrent;
import rss.torrents.Episode;
import rss.torrents.UserTorrent;
import rss.user.User;

import java.util.Collection;

/**
 * User: dikmanm
 * Date: 16/10/2015 23:05
 */
public interface UserEpisodeTorrentDao {

    UserEpisodeTorrent findUserEpisodeTorrent(User user, long torrentId);

    Collection<UserTorrent> findUserEpisodes(long userId, Collection<Episode> episodes);
}
