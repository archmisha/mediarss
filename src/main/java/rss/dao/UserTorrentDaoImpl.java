package rss.dao;

import org.springframework.stereotype.Repository;
import rss.entities.Episode;
import rss.entities.Movie;
import rss.entities.User;
import rss.entities.UserTorrent;
import rss.util.DateUtils;

import java.util.*;

/**
 * User: Michael Dikman
 * Date: 24/05/12
 * Time: 11:05
 */
@Repository
public class UserTorrentDaoImpl extends BaseDaoJPA<UserTorrent> implements UserTorrentDao {

	@Override
	public List<UserTorrent> findUserMoviesForUserFeed(Date dateUploaded, User user) {
		Map<String, Object> params = new HashMap<>(2);
		params.put("dateUploaded", dateUploaded);
		params.put("userId", user.getId());
		return super.findByNamedQueryAndNamedParams("UserMovieTorrent.findUserMoviesForUserFeed", params);
	}

	@Override
	public List<UserTorrent> findEpisodesAddedSince(User user, Date dateAdded) {
		Map<String, Object> params = new HashMap<>(2);
		params.put("dateAdded", dateAdded);
		params.put("userId", user.getId());
		return super.findByNamedQueryAndNamedParams("UserEpisodeTorrent.findEpisodesAddedSince", params);
	}

	@Override
	public List<UserTorrent> findScheduledUserMovies(User user, int backlogDays) {
		Map<String, Object> params = new HashMap<>(2);
		params.put("userId", user.getId());
		params.put("downloadDate", DateUtils.getPastDate(backlogDays));
		return super.findByNamedQueryAndNamedParams("UserMovieTorrent.findScheduledUserMovies", params);
	}

	@Override
	public List<Long> findScheduledUserMoviesCount(User user, int backlogDays) {
		Map<String, Object> params = new HashMap<>(2);
		params.put("userId", user.getId());
		params.put("downloadDate", DateUtils.getPastDate(backlogDays));
		return super.findByNamedQueryAndNamedParams("UserMovieTorrent.findScheduledUserMoviesIds", params);
	}

	@Override
	public List<UserTorrent> findUserEpisodeTorrentByTorrentId(long torrentId) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("torrentId", torrentId);
		return super.findByNamedQueryAndNamedParams("UserEpisodeTorrent.findUserTorrentByTorrentId2", params);
	}

	@Override
	public Collection<UserTorrent> findUserEpisodes(Collection<Episode> episodes, User user) {
		if (episodes.isEmpty()) {
			return Collections.emptyList();
		}

		StringBuilder query = new StringBuilder();
		List<Object> params = new ArrayList<>();

		query.append("select t from UserEpisodeTorrent as t where user.id = :p0 and t.torrent.id in (");
		params.add(user.getId());
		int counter = 1;
		for (Episode episode : episodes) {
			for (Long torrentId : episode.getTorrentIds()) {
				query.append(":p").append(counter++).append(",");
				params.add(torrentId);
			}
		}

		// if no torrent ids for the given episodes
		if (params.isEmpty()) {
			return Collections.emptyList();
		}

		query.setCharAt(query.length() - 1, ')');

		return find(query.toString(), params.toArray());
	}

	@Override
	public Collection<UserTorrent> findUserMovies(User user, Collection<Movie> movies) {
		if (movies.isEmpty()) {
			return Collections.emptyList();
		}

		StringBuilder query = new StringBuilder();
		List<Object> params = new ArrayList<>();

		query.append("select t from UserMovieTorrent as t where t.user.id = :p0 and t.torrent.id in (");
		params.add(user.getId());
		int counter = 1;
		for (Movie movie : movies) {
			for (Long torrentId : movie.getTorrentIds()) {
				query.append(":p").append(counter++).append(",");
				params.add(torrentId);
			}
		}
		query.setCharAt(query.length() - 1, ')');

		return find(query.toString(), params.toArray());
	}
}
