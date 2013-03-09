package rss;

/**
 * User: dikmanm
 * Date: 08/02/13 12:10
 */
public class MediaRSSException extends RuntimeException {

	public MediaRSSException() {
		super();
	}

	public MediaRSSException(String message) {
		super(message);
	}

	public MediaRSSException(String message, Throwable cause) {
		super(message, cause);
	}

	public MediaRSSException(Throwable cause) {
		super(cause);
	}

	protected MediaRSSException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
