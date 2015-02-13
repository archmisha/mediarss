package rss.util;

import java.util.Locale;

/**
 * User: Michael Dikman
 * Date: 11/05/12
 * Time: 14:59
 */
public class JspUtil {

	public static String getLoadingI18N(String locale) {
		rss.util.ServerMessageProvider messageProvider = rss.util.ServerMessageProvider.get();
		String loading = messageProvider.getString("loading", new Locale(locale));
		return loading;
	}
}
