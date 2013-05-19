package rss.controllers.vo;

import rss.entities.Torrent;
import rss.entities.UserMovieTorrent;

/**
 * User: Michael Dikman
 * Date: 09/12/12
 * Time: 23:52
 */
public class UserMovieTorrentVO extends UserTorrentVO {

	private boolean viewed;
	private long movieId;

	public static UserMovieTorrentVO fromUserTorrent(UserMovieTorrent userTorrent) {
		return populate(new UserMovieTorrentVO(), userTorrent)
				.withMovieId(userTorrent.getUserMovie().getMovie().getId());
	}

	public static UserMovieTorrentVO fromTorrent(Torrent torrent, long movieId) {
		return populate(new UserMovieTorrentVO(), torrent)
				.withMovieId(movieId);
	}

	public UserMovieTorrentVO withViewed(boolean viewed) {
		this.viewed = viewed;
		return this;
	}

	public boolean isViewed() {
		return viewed;
	}

	public UserMovieTorrentVO withMovieId(long movieId) {
		this.movieId = movieId;
		return this;
	}

	public long getMovieId() {
		return movieId;
	}
}
