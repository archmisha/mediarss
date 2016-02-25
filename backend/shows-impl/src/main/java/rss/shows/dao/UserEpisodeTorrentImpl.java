package rss.shows.dao;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import rss.shows.UserEpisodeTorrent;
import rss.torrents.dao.UserTorrentImpl;

import javax.persistence.UniqueConstraint;

/**
 * User: dikmanm
 * Date: 18/10/2015 20:58
 */
@javax.persistence.Entity(name = "UserEpisodeTorrent")
@org.hibernate.annotations.Table(appliesTo = "user_episode_torrent", indexes = {
        @Index(name = "user_episode_torrent_userId_added_idx", columnNames = {"user_id", "added"})
})
@javax.persistence.Table(name = "user_episode_torrent",
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
public class UserEpisodeTorrentImpl extends UserTorrentImpl implements UserEpisodeTorrent {

    private static final long serialVersionUID = 8134537615057339812L;
}
