package rss.torrents.searchers.simple;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.log.LogService;
import rss.torrents.Torrent;
import rss.torrents.dao.TorrentImpl;
import rss.torrents.requests.MediaRequest;
import rss.torrents.searchers.SearchResult;
import rss.torrents.searchers.SimpleTorrentSearcher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Michael Dikman
 * Date: 04/12/12
 * Time: 19:17
 */
@Service("publichdSearcher")
public class PublichdSearcher<T extends MediaRequest> extends SimpleTorrentSearcher<T> {

	public static final String NAME = "publichd";
	public static final Pattern PATTERN = Pattern.compile("<tag:torrents\\[\\].download /><a href=\".*?\">(.*?)<a href=(.*?)>.*AddDate</b></td>.*?>(.*?)</td>.*?seeds: (\\d+)", Pattern.DOTALL);

	@Autowired
	protected LogService logService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getPriority() {
		return -1;
	}

	@Override
	public String getDefaultDomain() {
		return "https://privatehd.to/";
	}

	@Override
	protected Collection<String> getEntryUrl() {
		Collection<String> res = new ArrayList<>();
		for (String domain : searcherConfigurationService.getSearcherConfiguration(getName()).getDomains()) {
			res.add(domain + "/index.php?page=torrent-details&id=");
		}
		return res;
	}

	@Override
	protected Collection<String> getSearchUrl(T mediaRequest) {
		// todo: currently not handling search
		throw new UnsupportedOperationException();
	}

	@Override
	public SearchResult search(T mediaRequest) {
		// todo: currently not handling search
		return SearchResult.createNotFound();
	}

//	@Override
//	protected String getSearchByIdUrl(T mediaRequest) {
//		// no need to encode the hash
//		if (mediaRequest.getHash() != null) {
//			return ENTRY_URL + mediaRequest.getHash();
//		}
//		return null;
//	}

	@Override
	public String parseId(MediaRequest mediaRequest, String page) {
		return mediaRequest.getHash();
	}

	@Override
	protected List<Torrent> parseSearchResultsPage(String url, T mediaRequest, String page) {
		return Collections.emptyList();
	}

	@Override
	protected Torrent parseTorrentPage(T mediaRequest, String page) {
		Matcher matcher = PATTERN.matcher(page);
		if (!matcher.find()) {
			if (!page.contains("Bad ID!")) { // in that case just id not found - not a parsing problem
				logService.error(getClass(), "Failed parsing page of '" + mediaRequest.toString() + "': " + page);
			}
			return null;
		}

		String title = matcher.group(1).trim(); // sometimes comes with line break at the end - ruins log
		title = StringEscapeUtils.unescapeHtml4(title);
		String link = matcher.group(2);
		String uploadDataString = matcher.group(3);
		int seeders = Integer.parseInt(matcher.group(4));

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date uploadDate = null;
		try {
			uploadDate = formatter.parse(uploadDataString);
		} catch (ParseException e) {
			logService.error(getClass(), "Failed parsing date '" + uploadDataString + "': " + e.getMessage(), e);
		}

		Torrent torrent = new TorrentImpl(title, link, uploadDate, seeders);
		torrent.setImdbId(parseImdbUrl(page, title));
		return torrent;
	}
}
