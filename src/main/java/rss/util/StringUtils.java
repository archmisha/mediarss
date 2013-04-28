package rss.util;

/**
 * User: Michael Dikman
 * Date: 22/12/12
 * Time: 15:44
 */
public class StringUtils {

	public static int indexOf(String seq, String searchSeq, int defaultValue) {
		int index = searchSeq.indexOf(seq);
		if (index == -1) {
			index = defaultValue;
		}
		return index;
	}
}
