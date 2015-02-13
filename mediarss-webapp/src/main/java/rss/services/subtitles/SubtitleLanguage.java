package rss.services.subtitles;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: dikmanm
 * Date: 24/01/13 21:42
 */
public enum SubtitleLanguage {
	HEBREW {
		public String toString() {
			return "עברית";
		}
	}, ENGLISH {
		public String toString() {
			return "English";
		}
	};

	public static SubtitleLanguage fromString(String languageName) {
		for (SubtitleLanguage subtitleLanguage : SubtitleLanguage.values()) {
			if (subtitleLanguage.toString().equals(languageName)) {
				return subtitleLanguage;
			}
		}

		return null;
		//throw new RuntimeException(languageName);
	}

	public static Collection<String> getValues() {
		Collection<String> result = new ArrayList<>();
		for (SubtitleLanguage subtitleLanguage : values()) {
			result.add(subtitleLanguage.toString());
		}
		return result;
	}
}
