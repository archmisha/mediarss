package rss.util;

import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * User: dikmanm
 * Date: 26/04/13 18:50
 */
public class Utils {

	public static Throwable getRootCause(Throwable e) {
		Throwable rootCause = ExceptionUtils.getRootCause(e);
		if (rootCause == null) {
			rootCause = e;
		}
		return rootCause;
	}

	@SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
	public static boolean isRootCauseMessageContains(Throwable e, String msg) {
		Throwable rootCause = getRootCause(e);
		return rootCause.getMessage() != null && rootCause.getMessage().contains(msg);
	}
}
