package rss;

/**
 * User: dikmanm
 * Date: 08/02/13 12:10
 */
public class ShowNotFoundException extends MediaRSSException {

	public ShowNotFoundException() {
		super();
	}

	public ShowNotFoundException(String message) {
		super(message);
	}

	public ShowNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ShowNotFoundException(Throwable cause) {
		super(cause);
	}

	protected ShowNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
