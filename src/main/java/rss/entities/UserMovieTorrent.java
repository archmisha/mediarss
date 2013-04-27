package rss.entities;

import javax.persistence.*;

/**
 * User: dikmanm
 * Date: 12/02/13 21:06
 */
@SuppressWarnings("UnusedDeclaration")
@Entity
@Table(name = "user_movie_torrent")
@NamedQueries({
		@NamedQuery(name = "UserMovieTorrent.findUserMoviesForUserFeed",
				query = "select ut from UserMovieTorrent as ut " +
						"where ut.user.id = :userId and (ut.torrent.dateUploaded > :dateUploaded or downloadDate is null)"),
		@NamedQuery(name = "UserMovieTorrent.findScheduledUserMovies",
				query = "select ut from UserMovieTorrent as ut " +
						"where ut.user.id = :userId and (downloadDate is null or downloadDate > :downloadDate)")
//		@NamedQuery(name = "UserMovieTorrent.findUserTorrentByTorrentId",
//				query = "select ut from UserMovieTorrent as ut where ut.user.id = :userId and ut.torrent.id = :torrentId")
})
public class UserMovieTorrent extends UserTorrent {
	private static final long serialVersionUID = -6736397044745416876L;

	@ManyToOne(targetEntity = UserMovie.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "user_movie_id")
	private UserMovie userMovie;

	public UserMovie getUserMovie() {
		return userMovie;
	}

	public void setUserMovie(UserMovie userMovie) {
		this.userMovie = userMovie;
	}
}
