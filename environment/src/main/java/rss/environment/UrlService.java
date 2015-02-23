package rss.environment;

/**
 * User: Michael Dikman
 * Date: 11/12/12
 * Time: 22:30
 */
public interface UrlService {

    public static final String MOVIES_RSS_FEED_TYPE = "movies";
    public static final String TV_SHOWS_RSS_FEED_TYPE = "shows";
    public static final String MEDIA_TYPE_URL_PARAMETER = "type";
    public static final String USER_ID_URL_PARAMETER = "user";
    public static final String HASH_URL_PARAMETER = "hash";
	public static final String USER_FEED_HASH_PARAMETER = "feedId";

    public static final String PERSONAL_RSS_FEED_URL = "generate/?" + USER_ID_URL_PARAMETER + "=%d&" + MEDIA_TYPE_URL_PARAMETER + "=%s&" + USER_FEED_HASH_PARAMETER + "=%s";

    String getApplicationUrl();
}
