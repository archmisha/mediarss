package rss.entities;

import javax.persistence.*;
import java.util.Date;

/**
 * User: dikmanm
 * Date: 12/02/13 21:06
 */
@SuppressWarnings("UnusedDeclaration")
@Entity
@Table(name = "user_movie")
@NamedQueries({
		@NamedQuery(name = "UserMovie.findUserMovie",
				query = "select t from UserMovie as t " +
						"where t.user.id = :userId and t.movie.id = :movieId"),
		@NamedQuery(name = "UserMovie.findFutureUserMovies",
				query = "select um from UserMovie as um join um.movie as m " +
						"where um.user.id = :userId and m.torrentIds.size = 0"),
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
}
