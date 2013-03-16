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

	List<UserTorrent> findEpisodesAddedSince(Date dateUploaded, User user);

	UserTorrent findMovieUserTorrentByTorrentId(long torrentId, User user);

	UserTorrent findEpisodeUserTorrentByTorrentId(long torrentId, User user);

	Collection<UserTorrent> findUserEpisodes(Collection<Episode> episodes, User user);

	Collection<UserTorrent> findUserMovies(Collection<Movie> movies, User user);
}
