package rss.movies;

import rss.torrents.UserTorrent;

/**
 * User: dikmanm
 * Date: 12/02/13 21:06
 */
public interface UserMovieTorrent extends UserTorrent {

	UserMovie getUserMovie();

	void setUserMovie(UserMovie userMovie);
}
