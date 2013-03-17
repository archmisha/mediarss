package rss.services.parsers;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import rss.services.downloader.MovieRequest;

import java.util.Calendar;
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
public class TorrentzParser implements PageParser {

	private static Log log = LogFactory.getLog(TorrentzParser.class);

	// need to skip entries like /z/...
	public static final Pattern ENTRY_CONTENT_PATTERN = Pattern.compile("<dl><dt><a href=\"/(\\w+)\">(.*?)</a> &#187; (.*?)</dt>.*?<span class=\"s\">(.*?)</span>.*?</dl>");

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

			name = stripTags(name);
			name = StringEscapeUtils.unescapeHtml4(name);
			type = stripTags(type);

			boolean skip = false;
			for (String keyword : TYPES_TO_SKIP) {
				if (type.contains(keyword)) {
					log.info("Skipping movie '" + name + "' due to type: '" + type + "'");
					skip = true;
				}
			}

			if (!skip) {
				movies.add(new MovieRequest(name, hash));
			}
		}

		return (Set<T>) movies;
	}

	private String stripTags(String name) {
		name = name.replaceAll("<b>", "");
		name = name.replaceAll("</b>", "");
		return name;
	}
}
