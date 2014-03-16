package rss.dao;

import rss.entities.*;

import java.util.Collection;
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

	Collection<UserMovieTorrent> findUserMovieTorrents(User user, Collection<Movie> movies);

	List<UserTorrent> findUserEpisodeTorrentByTorrentId(long torrentId);

	UserMovieTorrent findUserMovieTorrent(User user, long torrentId);

	UserEpisodeTorrent findUserEpisodeTorrent(User user, long torrentId);

	Collection<UserTorrent> findUserEpisodes(long userId, Collection<Episode> episodes);
}
