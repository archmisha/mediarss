package rss.entities;

import org.hibernate.annotations.Index;
import rss.SubtitleLanguage;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 25/11/12
 * Time: 11:58
 */
@Entity
@Table(name = "user")
@NamedQueries({
		@NamedQuery(name = "User.findByEmail",
				query = "select b from User as b where lower(b.email) = :email"),
		@NamedQuery(name = "User.findByTrackedShow",
				query = "select b from User as b inner join b.shows as s where s.id = :showId"),
		@NamedQuery(name = "User.getUsersCountTrackingShow",
				query = "select count(b) from User as b inner join b.shows as s where s.id = :showId"),
		@NamedQuery(name = "User.getEpisodesToDownload",
				query = "select e from User as u inner join u.shows as s inner join s.episodes as e " +
						"where u.id = :userId and e.airDate > :fromDate")
})
public class User extends BaseEntity {

	private static final long serialVersionUID = -8612852113678825310L;

	@Column(name = "email", unique = true)
	@Index(name = "email_idx")
	private String email;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "password")
	private String password;

	@Column(name = "validation_hash")
	private String validationHash;

	@Column(name = "feed_hash")
	private String feedHash;

	@Column(name = "created")
	private Date created;

	@Column(name = "last_shows_feed_generated")
	private Date lastShowsFeedGenerated;

	@Column(name = "last_movies_feed_generated")
	private Date lastMoviesFeedGenerated;

	@Column(name = "last_login")
	private Date lastLogin;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "user_shows",
			joinColumns = {@JoinColumn(name = "user_id")},
			inverseJoinColumns = {@JoinColumn(name = "show_id")}
	)
	private Set<Show> shows;

	@Column(name = "subtitles")
	private SubtitleLanguage subtitles;

	public User() {
		shows = new HashSet<>();
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getLastShowsFeedGenerated() {
		return lastShowsFeedGenerated;
	}

	public void setLastShowsFeedGenerated(Date lastShowsFeedGenerated) {
		this.lastShowsFeedGenerated = lastShowsFeedGenerated;
	}

	public Date getLastMoviesFeedGenerated() {
		return lastMoviesFeedGenerated;
	}

	public void setLastMoviesFeedGenerated(Date lastMoviesFeedGenerated) {
		this.lastMoviesFeedGenerated = lastMoviesFeedGenerated;
	}

	public boolean isValidated() {
		return validationHash == null;
	}

	public void setValidationHash(String validationHash) {
		this.validationHash = validationHash;
	}

	public String getValidationHash() {
		return validationHash;
	}

	@Override
	public String toString() {
		return email + " (userId=" + id + ")";
	}

	public Set<Show> getShows() {
		return shows;
	}

	public void setSubtitles(SubtitleLanguage subtitles) {
		this.subtitles = subtitles;
	}

	public SubtitleLanguage getSubtitles() {
		return subtitles;
	}

	public String getFeedHash() {
		return feedHash;
	}

	public void setFeedHash(String feedHash) {
		this.feedHash = feedHash;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}