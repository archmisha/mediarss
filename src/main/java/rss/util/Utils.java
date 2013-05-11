package rss.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

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

	public static boolean isCauseMessageContains(Exception e, String msg) {
		for (Throwable throwable : ExceptionUtils.getThrowableList(e)) {
			if (throwable.getMessage() != null && throwable.getMessage().contains(msg)) {
				return true;
			}
		}
		return false;
	}
}
