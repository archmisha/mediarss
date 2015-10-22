package rss.movies.dao;

import rss.ems.dao.Dao;
import rss.movies.UserMovieTorrent;
import rss.torrents.Movie;
import rss.user.User;

import java.util.Collection;

/**
 * User: dikmanm
 * Date: 16/10/2015 23:03
 */
public interface UserMovieTorrentDao extends Dao<UserMovieTorrent> {

    Collection<UserMovieTorrent> findUserMovieTorrents(User user, Collection<Movie> movies);

    UserMovieTorrent findUserMovieTorrent(User user, long torrentId);
}
