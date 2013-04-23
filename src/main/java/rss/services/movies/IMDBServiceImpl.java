package rss.services.movies;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.MediaRSSException;
import rss.entities.Movie;
import rss.services.PageDownloader;
import rss.services.log.LogService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: dikmanm
 * Date: 21/04/13 21:54
 */
@Service
public class IMDBServiceImpl implements IMDBService {

	public static final Pattern NAME_YEAR_PATTERN = Pattern.compile("\\(?[^\\d]*(\\d+)[^\\d]*\\)?");
	public static final Pattern COMING_SOON_PATTERN = Pattern.compile("<div class=\"showtime\">.*?<h2>Coming Soon</h2>", Pattern.MULTILINE | Pattern.DOTALL);
	public static final Pattern NOT_YET_RELEASED_PATTERN = Pattern.compile("<div class=\"rating-ineligible\">.*?Not yet released.*?</div>", Pattern.MULTILINE | Pattern.DOTALL);
	public static final Pattern VIEWERS_PATTERN = Pattern.compile("<span itemprop=\"ratingCount\">([^<]*)</span>");
	public static final Pattern OLD_YEAR_PATTERN = Pattern.compile("<meta name=\"title\" content=\"(.*?) - IMDb\" />");

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	private LogService logService;

	@Override
	public IMDBParseResult downloadMovieFromIMDB(String imdbUrl) {
		long from = System.currentTimeMillis();
		String partialPage;
		try {
			// imdb pages are large, downloading until a regular expression is satisfied and that chunk is returned
			partialPage = pageDownloader.downloadPageUntilFound(imdbUrl, VIEWERS_PATTERN);
		} catch (Exception e) {
			// usually it is HTTP/1.1 404 Not Found
			if (!e.getMessage().contains("404 Not Found")) {
				logService.error(getClass(), "Failed downloading IMDB page " + imdbUrl + ": " + e.getMessage(), e);
			}
			return IMDBParseResult.createNotFound(imdbUrl);
		}

		// check for old year
		// <meta name="title" content="The Prestige (2006) - IMDb" />
		Matcher oldYearMatcher = OLD_YEAR_PATTERN.matcher(partialPage);
		oldYearMatcher.find();
		String name = oldYearMatcher.group(1);
		name = StringEscapeUtils.unescapeHtml4(name);

		logService.info(getClass(), String.format("Downloading title for movie '%s' took %d millis", name, (System.currentTimeMillis() - from)));

		Matcher comingSoonMatcher = COMING_SOON_PATTERN.matcher(partialPage);
		boolean isComingSoon = comingSoonMatcher.find();

		if (!isComingSoon) {
			Matcher notYetReleasedMatcher = NOT_YET_RELEASED_PATTERN.matcher(partialPage);
			isComingSoon = notYetReleasedMatcher.find();
		}

		int viewers = -1;
		// if not yet released, no point parsing for viewers
		if (!isComingSoon) {
			Matcher viewersMatcher = VIEWERS_PATTERN.matcher(partialPage);
			if (!viewersMatcher.find()) {
				logService.warn(getClass(), "Failed retrieving number of viewers for '" + name + "': " + partialPage);
			} else {
				String viewersStr = viewersMatcher.group(1);
				viewersStr = viewersStr.replaceAll(",", "");
				viewers = Integer.parseInt(viewersStr);
			}
		}

		return IMDBParseResult.createFound(imdbUrl, name, parseMovieYear(name), isComingSoon, viewers);
	}

	private int parseMovieYear(String name) {
		Matcher matcher = NAME_YEAR_PATTERN.matcher(name);
		if (!matcher.find()) {
			throw new MediaRSSException("Failed parsing movie year for movie '" + name + "'");
		}

		return Integer.parseInt(matcher.group(1));
	}
}
