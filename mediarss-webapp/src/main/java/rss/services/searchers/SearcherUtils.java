package rss.services.searchers;

import rss.entities.MediaQuality;

/**
 * User: dikmanm
 * Date: 14/05/13 00:40
 */
public class SearcherUtils {

	public static MediaQuality findQuality(String str) {
		str = str.toLowerCase();
		for (MediaQuality mediaQuality : MediaQuality.values()) {
			// skipping normal, cuz its "" - will always match
			if (mediaQuality != MediaQuality.NORMAL && str.contains(mediaQuality.toString())) {
				return mediaQuality;
			}
		}
		return MediaQuality.NORMAL;
	}
}
