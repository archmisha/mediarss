package rss;

/**
 * User: dikmanm
 * Date: 08/02/13 12:10
 */
public class EmailAlreadyRegisteredException extends RegisterException {

	public EmailAlreadyRegisteredException() {
		super();    //To change body of overridden methods use File | Settings | File Templates.
	}

	public EmailAlreadyRegisteredException(String message) {
		super(message);    //To change body of overridden methods use File | Settings | File Templates.
	}

	public EmailAlreadyRegisteredException(String message, Throwable cause) {
		super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
	}

	public EmailAlreadyRegisteredException(Throwable cause) {
		super(cause);    //To change body of overridden methods use File | Settings | File Templates.
	}

	protected EmailAlreadyRegisteredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);    //To change body of overridden methods use File | Settings | File Templates.
	}
}
