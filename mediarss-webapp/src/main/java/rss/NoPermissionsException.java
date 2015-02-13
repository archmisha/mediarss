package rss;

/**
 * User: dikmanm
 * Date: 08/02/13 11:59
 */
public class NoPermissionsException extends RuntimeException {

	public NoPermissionsException() {
		super();
	}

	public NoPermissionsException(String message) {
		super(message);
	}

	public NoPermissionsException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoPermissionsException(Throwable cause) {
		super(cause);
	}

	protected NoPermissionsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
