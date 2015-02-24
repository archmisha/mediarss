package rss;

/**
 * User: dikmanm
 * Date: 02/02/13 14:55
 */
public class RecoverableConnectionException extends PageDownloadException {

	public RecoverableConnectionException() {
		super();    //To change body of overridden methods use File | Settings | File Templates.
	}

	public RecoverableConnectionException(String message) {
		super(message);    //To change body of overridden methods use File | Settings | File Templates.
	}

	public RecoverableConnectionException(String message, Throwable cause) {
		super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
	}

	public RecoverableConnectionException(Throwable cause) {
		super(cause);    //To change body of overridden methods use File | Settings | File Templates.
	}

	protected RecoverableConnectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);    //To change body of overridden methods use File | Settings | File Templates.
	}
}
