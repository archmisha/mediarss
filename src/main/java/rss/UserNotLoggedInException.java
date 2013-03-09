package rss;

/**
 * User: dikmanm
 * Date: 25/01/13 10:38
 */
public class UserNotLoggedInException extends RuntimeException {

	private static final long serialVersionUID = 2053225641448099385L;

	public UserNotLoggedInException() {
	}

	public UserNotLoggedInException(String message) {
		super(message);
	}

	public UserNotLoggedInException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserNotLoggedInException(Throwable cause) {
		super(cause);
	}
}
