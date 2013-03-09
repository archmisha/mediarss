package rss.controllers.vo;

/**
 * User: dikmanm
 * Date: 02/02/13 15:18
 */
public class RssImportResponse /*implements IsSerializable*/ {

	private UserResponse user;
	private String errorMessage;

	public UserResponse getUser() {
		return user;
	}

	public void setUser(UserResponse user) {
		this.user = user;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public RssImportResponse withErrorMessage(String msg) {
		this.errorMessage = msg;
		return this;
	}

	public RssImportResponse withUser(UserResponse userResponse) {
		this.user = userResponse;
		return  this;
	}
}
