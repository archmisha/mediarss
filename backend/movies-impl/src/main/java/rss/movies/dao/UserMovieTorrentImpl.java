package rss.movies.dao;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.Table;
import rss.movies.UserMovie;
import rss.movies.UserMovieTorrent;
import rss.torrents.dao.UserTorrentImpl;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.UniqueConstraint;

/**
 * User: dikmanm
 * Date: 18/10/2015 20:24
 */
@SuppressWarnings("UnusedDeclaration")
@javax.persistence.Entity(name = "UserMovieTorrent")
@Table(appliesTo = "user_movie_torrent",
        indexes = {
                @Index(name = "umt_userId_downloadDate_idx", columnNames = {"user_id", "download_date"})
        })
@javax.persistence.Table(name = "user_movie_torrent",
        uniqueConstraints = {
                @UniqueConstraint(name = "umt_userId_torrentId_idx", columnNames = {"user_id", "torrent_id"})
        })
@NamedQueries({
        @NamedQuery(name = "UserMovieTorrent.findUserMoviesForUserFeed",
                query = "select ut from UserMovieTorrent as ut " +
                        "where ut.user.id = :userId and (ut.torrent.dateUploaded > :dateUploaded or downloadDate is null)"),
        @NamedQuery(name = "UserMovieTorrent.findUserMovieTorrents",
                query = "select ut from UserMovieTorrent as ut " +
                        "where ut.user.id = :userId and ut.userMovie.movie.id in (:movieIds)"),
        @NamedQuery(name = "UserMovieTorrent.findUserMovieTorrentsByTorrentIds",
                query = "select ut from UserMovieTorrent as ut where ut.user.id = :userId and ut.torrent.id in (:torrentIds)")
})
public class UserMovieTorrentImpl extends UserTorrentImpl implements UserMovieTorrent {
    private static final long serialVersionUID = -6736397044745416876L;

    @ManyToOne(targetEntity = UserMovieImpl.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_movie_id")
    private UserMovie userMovie;

    public UserMovie getUserMovie() {
        return userMovie;
    }

    public void setUserMovie(UserMovie userMovie) {
        this.userMovie = userMovie;
    }
}
