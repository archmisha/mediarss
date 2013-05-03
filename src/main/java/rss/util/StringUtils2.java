package rss.util;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 15:44
 */
public class StringUtils2 {

	private static Map<Integer, Pattern> patterns;

	static {
		patterns = new HashMap<>();
		patterns.put(Calendar.MINUTE, Pattern.compile("(\\d+)\\s+minutes?", Pattern.DOTALL));
		patterns.put(Calendar.HOUR_OF_DAY, Pattern.compile("(\\d+)\\s+hours?", Pattern.DOTALL));
		patterns.put(Calendar.DAY_OF_MONTH, Pattern.compile("(\\d+)\\s+days?", Pattern.DOTALL));
		patterns.put(Calendar.WEEK_OF_MONTH, Pattern.compile("(\\d+)\\s+weeks?", Pattern.DOTALL));
		patterns.put(Calendar.MONTH, Pattern.compile("(\\d+)\\s+months?", Pattern.DOTALL));
		patterns.put(Calendar.YEAR, Pattern.compile("(\\d+)\\s+years?", Pattern.DOTALL));
	}

	public static int indexOf(String seq, String searchSeq, int defaultValue) {
		int index = searchSeq.indexOf(seq);
		if (index == -1) {
			index = defaultValue;
		}
		return index;
	}

	// 1 week 6 days ago, 17 minutes ago, 1 month ago, 3 years 5 months ago, 2 months 1 week ago, 21 hours 15 minutes ago
	// 1 day 55 minutes ago, 20 hours 28 seconds ago
	// ignoring the seconds part
	public static Date parseDateUploaded(String str) {
		str = str.trim();

		// remove the ago suffix
		String suffix = " ago";
		if (str.endsWith(suffix)) {
			str = str.substring(0, str.length() - suffix.length());
		}

		Calendar now = Calendar.getInstance();
		for (Map.Entry<Integer, Pattern> entry : patterns.entrySet()) {
			Matcher matcher = entry.getValue().matcher(str);
			if (matcher.find()) {
				int value = Integer.parseInt(matcher.group(1));
				now.add(entry.getKey(), -value);
			}
		}
		return now.getTime();
	}
}
