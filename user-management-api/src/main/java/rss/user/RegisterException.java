package rss.user;

import rss.MediaRSSException;

/**
 * User: dikmanm
 * Date: 08/02/13 11:59
 */
public class RegisterException extends MediaRSSException {

	public RegisterException() {
		super();
	}

	public RegisterException(String message) {
		super(message);
	}

	public RegisterException(String message, Throwable cause) {
		super(message, cause);
	}

	public RegisterException(Throwable cause) {
		super(cause);
	}

	protected RegisterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
