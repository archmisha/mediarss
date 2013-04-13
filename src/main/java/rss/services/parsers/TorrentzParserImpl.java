package rss.services.parsers;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import rss.services.downloader.MovieRequest;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Michael Dikman
 * Date: 02/12/12
 * Time: 22:08
 * <p/>
 * <dl><dt><a href="/8683cfd2d2bea23dd137d806cb942755345a5e4d">Premium Rush 2012 1080p Blu ray Remux AVC DTS <b>HD</b> MA 5 1 KRaLiMaRKo
 * </a> &#187; bluray remux <b>movies</b> <b>hd</b> <b>video</b> <b>highres</b> x264</dt>
 * <dd><span class="v" style="color: #A2EB80" title="8">1</span><span class="a"><span title="Sun, 02 Dec 2012 00:06:45">19 hours</span></span>
 * <span class="s">17 GB</span> <span class="u">8</span><span class="d">128</span></dd></dl>
 */
@Service("torrentzParser")
public class TorrentzParserImpl implements TorrentzParser {

	private static Log log = LogFactory.getLog(TorrentzParserImpl.class);

	// need to skip entries like /z/...
	public static final Pattern ENTRY_CONTENT_PATTERN = Pattern.compile("<dl><dt><a href=\"/(\\w+)\">(.*?)</a> &#187; (.*?)</dt>.*?<span class=\"s\">(.*?)</span>.*?<span class=\"u\">(.*?)</span>.*?</dl>");

	private static final Pattern PIRATE_BAY_ID = Pattern.compile("http://thepiratebay[^/]+/torrent/([^\"/]+)");

	// no need in that already doing it in the search url
	private static final String[] TYPES_TO_SKIP = new String[]{"xxx", "porn", "brrip"};

	@SuppressWarnings("unchecked")
	@Override
	public <T> Set<T> parse(String page) {
		Set<MovieRequest> movies = new HashSet<>();
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
					log.info("Skipping movie '" + name + "' due to type: '" + type + "'");
					skip = true;
				}
			}

			if (!skip) {
				MovieRequest movieRequest = new MovieRequest(name, hash);
				movieRequest.setUploaders(Integer.parseInt(uploaders));
				movies.add(movieRequest);
			}
		}

		return (Set<T>) movies;
	}

	private String stripTags(String name) {
		name = name.replaceAll("<b>", "");
		name = name.replaceAll("</b>", "");
		return name;
	}

	public String getPirateBayId(String page) {
		Matcher pirateBayIdMatcher = PIRATE_BAY_ID.matcher(page);
		if (pirateBayIdMatcher.find()) {
			return pirateBayIdMatcher.group(1);
		}
		return null;
	}
}
