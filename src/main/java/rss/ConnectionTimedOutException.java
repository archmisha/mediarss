package rss;

/**
 * User: dikmanm
 * Date: 02/02/13 14:55
 */
public class ConnectionTimedOutException extends PageDownloadException {

	public ConnectionTimedOutException() {
		super();    //To change body of overridden methods use File | Settings | File Templates.
	}

	public ConnectionTimedOutException(String message) {
		super(message);    //To change body of overridden methods use File | Settings | File Templates.
	}

	public ConnectionTimedOutException(String message, Throwable cause) {
		super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
	}

	public ConnectionTimedOutException(Throwable cause) {
		super(cause);    //To change body of overridden methods use File | Settings | File Templates.
	}

	protected ConnectionTimedOutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);    //To change body of overridden methods use File | Settings | File Templates.
	}
}
