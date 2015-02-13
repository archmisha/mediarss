package rss.controllers.vo;

import java.util.Date;

/**
 * User: dikmanm
 * Date: 07/02/13 18:46
 */
public class UserVO {

	private long id;
	private boolean isLoggedIn;
	private String email;
	private String firstName;
	private String lastName;
	private boolean admin;
	private String subtitles;
	private Date lastLogin;
	private Date lastShowsFeedGenerated;
	private Date lastMoviesFeedGenerated;

	public String getFirstName() {
		return firstName;
	}

	public String getEmail() {
		return email;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public String getSubtitles() {
		return subtitles;
	}

	public void setSubtitles(String subtitles) {
		this.subtitles = subtitles;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}

	public UserVO withLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
		return this;
	}

	public UserVO withId(long id) {
		this.id = id;
		return this;
	}

	public UserVO withEmail(String email) {
		this.email = email;
		return this;
	}

	public UserVO withAdmin(boolean isAdmin) {
		this.admin = isAdmin;
		return this;
	}

	public UserVO withSubtitles(String subtitles) {
		this.subtitles = subtitles;
		return this;
	}

	public UserVO withFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public UserVO withLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	public UserVO withLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
		return this;
	}

	public UserVO withLastShowsFeedAccess(Date lastShowsFeedGenerated) {
		this.lastShowsFeedGenerated = lastShowsFeedGenerated;
		return this;
	}

	public UserVO withLastMoviesFeedAccess(Date lastMoviesFeedGenerated) {
		this.lastMoviesFeedGenerated = lastMoviesFeedGenerated;
		return this;
	}

	public String getLastName() {
		return lastName;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public Date getLastShowsFeedGenerated() {
		return lastShowsFeedGenerated;
	}

	public Date getLastMoviesFeedGenerated() {
		return lastMoviesFeedGenerated;
	}
}
