package rss.entities;

import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * User: dikmanm
 * Date: 12/02/13 21:06
 */
@SuppressWarnings("UnusedDeclaration")
@Entity
@Table(name = "user_movie", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_id"}))
@org.hibernate.annotations.Table(appliesTo = "user_movie",
		indexes = {
				@Index(name = "user_movie_userId_movieId_idx", columnNames = {"user_id", "movie_id"})
		})
@NamedQueries({
		@NamedQuery(name = "UserMovie.findUserMovie",
				query = "select t from UserMovie as t " +
						"where t.user.id = :userId and t.movie.id = :movieId"),
		@NamedQuery(name = "UserMovie.findUserMoviesByMovieId",
				query = "select um from UserMovie as um join um.movie as m " +
						"where m.id = :movieId"),
		@NamedQuery(name = "UserMovie.findUserMovies",
				query = "select um from UserMovie as um join um.movie as m " +
						"where um.user.id = :userId and (m.torrentIds.size = 0 or (m.torrentIds.size > 0 and um.updated > :downloadDate))"),
		@NamedQuery(name = "UserMovie.findUserMoviesIds",
				query = "select m.id from UserMovie as um join um.movie as m " +
						"where um.user.id = :userId and (m.torrentIds.size = 0 or (m.torrentIds.size > 0 and um.updated > :downloadDate))"),
		@NamedQuery(name = "UserMovie.findUsersForFutureMovie",
				query = "select um.user from UserMovie as um join um.movie as m " +
						"where m.id = :movieId and m.torrentIds.size = 0")
})
public class UserMovie extends BaseEntity {

	private static final long serialVersionUID = 9040187622164970870L;

	@ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "updated")
	private Date updated;

	@ManyToOne(targetEntity = Movie.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "movie_id")
	private Movie movie;

	@OneToMany(mappedBy = "userMovie", targetEntity = UserMovieTorrent.class)
	private Set<UserMovieTorrent> userMovieTorrents;

	public UserMovie() {
		userMovieTorrents = new HashSet<>();
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public Movie getMovie() {
		return movie;
	}

	public void setMovie(Movie movie) {
		this.movie = movie;
	}

	public Set<UserMovieTorrent> getUserMovieTorrents() {
		return userMovieTorrents;
	}

	public void setUserMovieTorrents(Set<UserMovieTorrent> userMovieTorrents) {
		this.userMovieTorrents = userMovieTorrents;
	}
}
