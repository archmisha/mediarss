package rss.services.searchers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import rss.entities.Media;
import rss.entities.Torrent;
import rss.services.SearchResult;
import rss.services.requests.MediaRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Michael Dikman
 * Date: 04/12/12
 * Time: 19:17
 */
@Service("publichdSearcher")
public class PublichdSearcher<T extends MediaRequest, S extends Media> extends AbstractTorrentSearcher<T, S> {

	private static Log log = LogFactory.getLog(PublichdSearcher.class);

	public static final String NAME = "publichd.se";
	public static final String PUBLICHD_TORRENT_URL = "http://" + NAME + "/index.php?page=torrent-details&id=";
	public static final Pattern PATTERN = Pattern.compile("<tag:torrents\\[\\].download /><a href=\".*?\">(.*?)<a href=(.*?)>.*AddDate</b></td>.*?>(.*?)</td>.*?seeds: (\\d+)", Pattern.DOTALL);
	public static final Pattern IMDB_URL_PATTERN = Pattern.compile("www.imdb.com/title/(.*?)[\"/<]");

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected String getSearchUrl(T mediaRequest) {
		return PUBLICHD_TORRENT_URL + mediaRequest.getHash();
	}

	@Override
	protected SearchResult<S> parseSearchResults(T mediaRequest, String url, String page) {
		Matcher matcher = PATTERN.matcher(page);
		if (!matcher.find()) {
			if (!page.contains("Bad ID!")) { // in that case just id not found - not a parsing problem
				log.error("Failed parsing page of " + mediaRequest.toString() + ": " + page);
			}
			return SearchResult.createNotFound();
		}

		String title = matcher.group(1).trim(); // sometimes comes with line break at the end - ruins log
		String link = matcher.group(2);
		String uploadDataString = matcher.group(3);
		int seeders = Integer.parseInt(matcher.group(4));

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date uploadDate = null;
		try {
			uploadDate = formatter.parse(uploadDataString);
		} catch (ParseException e) {
			log.error("Failed parsing date '" + uploadDataString + "': " + e.getMessage(), e);
		}

		String imdbUrl = null;
		matcher = IMDB_URL_PATTERN.matcher(page);
		if (matcher.find()) {
			imdbUrl = "http://www.imdb.com/title/" + matcher.group(1);
		}

		Torrent movieTorrent = new Torrent(title, link, uploadDate, seeders);
		SearchResult<S> searchResult = new SearchResult<>(movieTorrent, NAME);
		searchResult.getMetaData().setImdbUrl(imdbUrl);

		if (isMatching(mediaRequest, searchResult)) {
			return searchResult;
		}
		return SearchResult.createNotFound();
	}

	protected boolean isMatching(T mediaRequest, SearchResult<S> searchResult) {
		if (searchResult.getTorrent().getTitle().toLowerCase().contains(mediaRequest.getTitle().toLowerCase())) {
			return true;
		}
		return false;
	}
}
