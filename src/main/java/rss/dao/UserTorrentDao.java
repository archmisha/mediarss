package rss.dao;

import rss.entities.Episode;
import rss.entities.Movie;
import rss.entities.User;
import rss.entities.UserTorrent;

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

	List<UserTorrent> findScheduledUserMovies(User user, int backlogDays);

//	UserTorrent findMovieUserTorrentByTorrentId(long torrentId, User user);

	List<UserTorrent> findUserEpisodeTorrentByTorrentId(long torrentId);

//	UserTorrent findEpisodeUserTorrentByTorrentId(long torrentId, User user);

	Collection<UserTorrent> findUserEpisodes(Collection<Episode> episodes, User user);

	Collection<UserTorrent> findUserMovies(User user, Collection<Movie> movies);

	List<Long> findScheduledUserMoviesCount(User user, int backlogDays);
}
