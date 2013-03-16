package rss.entities;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * User: dikmanm
 * Date: 12/02/13 21:06
 */
@SuppressWarnings("UnusedDeclaration")
@Entity
@Table(name = "movie_user_torrent")
@NamedQueries({
		@NamedQuery(name = "MovieUserTorrent.findUserMoviesForUserFeed",
				query = "select ut from MovieUserTorrent as ut " +
						"where ut.user.id = :userId and (ut.torrent.dateUploaded > :dateUploaded or downloadDate is null)"),
		@NamedQuery(name = "MovieUserTorrent.findUserTorrentByTorrentId",
				query = "select ut from MovieUserTorrent as ut where ut.user.id = :userId and ut.torrent.id = :torrentId")
})
public class MovieUserTorrent extends UserTorrent {
	private static final long serialVersionUID = -6736397044745416876L;
}
