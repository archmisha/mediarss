package rss;

/**
 * User: dikmanm
 * Date: 08/02/13 12:10
 */
public class MediaRSSException extends RuntimeException {

	private static final long serialVersionUID = 8632272983633107707L;

	private boolean log;
	private String userMessage;

	public MediaRSSException() {
		super();
		log = true;
	}

	public MediaRSSException(String message) {
		super(message);
		log = true;
	}

	public MediaRSSException(String message, Throwable cause) {
		super(message, cause);
		log = true;
	}

	public MediaRSSException(Throwable cause) {
		super(cause);
		log = true;
	}

	protected MediaRSSException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		log = true;
	}

	public MediaRSSException withUserMessage(String message) {
		this.userMessage = message;
		return this;
	}

	public MediaRSSException doNotLog() {
		log = false;
		return this;
	}

	public boolean log() {
		return log;
	}

	public String getUserMessage() {
		return this.userMessage == null ? this.getMessage() : this.userMessage;
	}
}
