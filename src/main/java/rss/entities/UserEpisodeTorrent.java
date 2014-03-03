package rss.entities;

import org.hibernate.annotations.Index;

import javax.persistence.*;

/**
 * User: dikmanm
 * Date: 12/02/13 21:07
 */
@Entity
@org.hibernate.annotations.Table(appliesTo = "user_episode_torrent", indexes = {
		@Index(name = "user_episode_torrent_userId_added_idx", columnNames = {"user_id", "added"})
})
@Table(name = "user_episode_torrent",
		uniqueConstraints = {
				@UniqueConstraint(name = "umt_userId_torrentId_idx2", columnNames = {"user_id", "torrent_id"})
		})
@NamedQueries({
		@NamedQuery(name = "UserEpisodeTorrent.findEpisodesAddedSince",
				query = "select ut from UserEpisodeTorrent as ut " +
						"where ut.user.id = :userId and ut.added >= :dateAdded"),
		@NamedQuery(name = "UserEpisodeTorrent.findUserTorrentByTorrentId",
				query = "select ut from UserEpisodeTorrent as ut where ut.torrent.id = :torrentId"),
		@NamedQuery(name = "UserEpisodeTorrent.findUserEpisodeTorrents",
				query = "select ut from UserEpisodeTorrent as ut where ut.user.id = :userId and ut.torrent.id in (:torrentIds)")
})
public class UserEpisodeTorrent extends UserTorrent {
	private static final long serialVersionUID = 8134537615057339812L;
}
