package rss.services.searchers.composite.torrentz;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.services.PageDownloader;
import rss.services.log.LogService;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: dikmanm
 * Date: 01/05/13 22:14
 */
@Service
public class TorrentzParserImpl implements TorrentzParser {

	public static final String NAME = "torrentz.eu";
	private static final String HOST_NAME = "http://" + NAME + "/";

	// need to skip entries like /z/...
//	public static final Pattern ENTRY_CONTENT_PATTERN = Pattern.compile("<dl><dt><a href=\"/(\\w+)\">(.*?)</a> &#187; (.*?)</dt>.*?<span class=\"s\">(.*?)</span>.*?<span class=\"u\">(.*?)</span>.*?</dl>");
	// adjusting to proxy, url wont start with relative path like that: /<hash> but will have <proxy url>/http://torrentz.eu/<hash>
	public static final Pattern ENTRY_CONTENT_PATTERN = Pattern.compile("<dl><dt><a href=\".*?([^/\"]+)\">(.*?)</a> &#187; (.*?)</dt>.*?<span class=\"s\">(.*?)</span>.*?<span class=\"u\">(.*?)</span>.*?</dl>");

	// removed filters: highres
	// size>2000m - filters dvdscr
	public static final String MOVIES_FILTERS = "+-shows+-porn+-brrip+-episodes+-music+size%3E2000m";

	public static final String TORRENTZ_ENTRY_URL = HOST_NAME;
	public static final String TORRENTZ_LATEST_MOVIES_URL = HOST_NAME + "search?f=movies+hd+video" + MOVIES_FILTERS + "+added%3A";
	public static final String TORRENTZ_MOVIE_SEARCH_URL = HOST_NAME + "search?f=movies+hd+video" + MOVIES_FILTERS + "+";
	public static final String TORRENTZ_EPISODE_SEARCH_URL = HOST_NAME + "verifiedP?f=";

	// no need in that already doing it in the search url
	private static final String[] TYPES_TO_SKIP = new String[]{"xxx", "porn", "brrip"};


	@Autowired
	protected LogService logService;

	@Autowired
	protected PageDownloader pageDownloader;

	public Set<TorrentzResult> downloadByUrl(String url) {
		String page = pageDownloader.downloadPage(url);
		Set<TorrentzResult> mediaRequests = parse(page);
		return mediaRequests;
	}

	@SuppressWarnings("unchecked")
	protected Set<TorrentzResult> parse(String page) {
		Set<TorrentzResult> movies = new HashSet<>();

		// cut out the sponsored links section before the results
		page = page.substring(page.indexOf("<div class=\"results\""));

		Matcher matcher = ENTRY_CONTENT_PATTERN.matcher(page);
		while (matcher.find()) {
			String hash = matcher.group(1);
			String name = matcher.group(2);
			String type = matcher.group(3);
			String size = matcher.group(4);
			String uploaders = matcher.group(5);

			name = stripTags(name);
			name = StringEscapeUtils.unescapeHtml4(name);
			type = stripTags(type);
			uploaders = uploaders.replaceAll(",", "");

			boolean skip = false;
			for (String keyword : TYPES_TO_SKIP) {
				if (type.contains(keyword)) {
					logService.info(getClass(), "Skipping movie '" + name + "' due to type: '" + type + "'");
					skip = true;
				}
			}

			if (!skip) {
				TorrentzResult movieRequest = new TorrentzResult(name, hash, Integer.parseInt(uploaders));
				movies.add(movieRequest);
			}
		}

		return movies;
	}

	private String stripTags(String name) {
		// avoiding usage of regex of String.replace method
		name = org.apache.commons.lang3.StringUtils.replace(name, "<b>", "");
		name = org.apache.commons.lang3.StringUtils.replace(name, "</b>", "");
		return name;
	}
}