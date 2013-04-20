package rss.entities;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * User: dikmanm
 * Date: 12/02/13 21:07
 */
@Entity
@Table(name = "user_episode_torrent")
@NamedQueries({
		@NamedQuery(name = "UserEpisodeTorrent.findEpisodesAddedSince",
				query = "select ut from UserEpisodeTorrent as ut " +
						"where ut.user.id = :userId and ut.added >= :dateAdded"),
		@NamedQuery(name = "UserEpisodeTorrent.findUserTorrentByTorrentId",
				query = "select ut from UserEpisodeTorrent as ut where ut.user.id = :userId and ut.torrent.id = :torrentId"),
		@NamedQuery(name = "UserEpisodeTorrent.findUserTorrentByTorrentId2",
				query = "select ut from UserEpisodeTorrent as ut where ut.torrent.id = :torrentId")
})
public class UserEpisodeTorrent extends UserTorrent {
	private static final long serialVersionUID = 8134537615057339812L;
}
