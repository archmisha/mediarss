package rss.torrents;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * User: dikmanm
 * Date: 16/10/2015 08:57
 */
public class TorrentUtils {

    private static final Pattern NORMALIZE_TO_NOTHING_PATTERN = Pattern.compile("['\"]");
    private static final Pattern NORMALIZE_TO_SPACE_PATTERN = Pattern.compile("[:&\\._\\+,\\(\\)!\\?/\\-]");
    private static final Pattern NORMALIZE_SPACES_PATTERN = Pattern.compile("\\s+");

    public static String normalize(String name) {
        name = name.toLowerCase();
        name = NORMALIZE_TO_NOTHING_PATTERN.matcher(name).replaceAll("");
        name = NORMALIZE_TO_SPACE_PATTERN.matcher(name).replaceAll(" ");
        // avoiding usage of regex of String.replace method
        name = StringUtils.replace(name, "and", " ");
        name = NORMALIZE_SPACES_PATTERN.matcher(name).replaceAll(" ");
        name = name.trim();
        return name;
    }

    // not normalizing: and, &
    public static String normalizeForQueryString(String name) {
        name = name.toLowerCase();
        name = NORMALIZE_TO_NOTHING_PATTERN.matcher(name).replaceAll("");
        name = NORMALIZE_TO_SPACE_PATTERN.matcher(name).replaceAll(" ");
        name = NORMALIZE_SPACES_PATTERN.matcher(name).replaceAll(" ");
        name = name.trim();
        return name;
    }
}
