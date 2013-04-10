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
@Table(name = "episode_user_torrent")
@NamedQueries({
		@NamedQuery(name = "EpisodeUserTorrent.findEpisodesAddedSince",
				query = "select ut from EpisodeUserTorrent as ut " +
						"where ut.user.id = :userId and ut.added >= :dateAdded"),
		@NamedQuery(name = "EpisodeUserTorrent.findUserTorrentByTorrentId",
				query = "select ut from EpisodeUserTorrent as ut where ut.user.id = :userId and ut.torrent.id = :torrentId"),
		@NamedQuery(name = "EpisodeUserTorrent.findUserTorrentByTorrentId2",
				query = "select ut from EpisodeUserTorrent as ut where ut.torrent.id = :torrentId")
})
public class EpisodeUserTorrent extends UserTorrent {
	private static final long serialVersionUID = 8134537615057339812L;
}
