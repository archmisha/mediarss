package rss.controllers.vo;

/**
 * User: dikmanm
 * Date: 07/02/13 18:46
 */
public class UserVO {

	private String firstName;
	private boolean admin;
	private String subtitles;

	public String getFirstName() {
		return firstName;
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

	public UserVO withEmail(String email) {
		this.firstName = email;
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
}
