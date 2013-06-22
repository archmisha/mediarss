package rss.services.searchers.simple;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import rss.entities.Torrent;
import rss.services.requests.MediaRequest;
import rss.services.searchers.SearchResult;
import rss.services.searchers.SimpleTorrentSearcher;
import rss.util.StringUtils2;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: dikmanm
 * Date: 04/05/13 16:40
 */
@Service("bitSnoopTorrentSearcher")
public class BitSnoopTorrentSearcher<T extends MediaRequest> extends SimpleTorrentSearcher<T> {

	public static final String NAME = "bitsnoop.com";
	private static final String ENTRY_URL = "http://" + NAME + "/";

	private static final Pattern BITSNOOP_TORRENTS_ID = Pattern.compile("http://bitsnoop.com/([^\"/]+)");

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getPriority() {
		return 3;
	}

	@Override
	protected Collection<String> getEntryUrl() {
		return Collections.singletonList(ENTRY_URL);
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

	@Override
	protected List<Torrent> parseSearchResultsPage(T mediaRequest, String page) {
		return Collections.emptyList();
	}

	@Override
	public String parseId(MediaRequest mediaRequest, String page) {
		Matcher matcher = BITSNOOP_TORRENTS_ID.matcher(page);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	@Override
	protected Torrent parseTorrentPage(T mediaRequest, String page) {
		try {
			Document doc = Jsoup.parse(page);
			String title = doc.select("#torrentHeader h1").get(0).text();
			String torrentUrl = doc.select("a[title=Magnet Link]").get(0).attr("href");
			String seedersStr = doc.select("#torrentHeader .seeders").html();
			int seeders = 0;
			if (!StringUtils.isBlank(seedersStr)) {
				seeders = Integer.parseInt(StringUtils.replace(seedersStr, ",", ""));
			}
			String[] arr = doc.select("#details li").get(0).html().split(" ");
			String hash = arr[arr.length - 1];
			String uploadedStr = doc.select("span[title=Added to index").html();

			Torrent torrent = new Torrent(title, torrentUrl, StringUtils2.parseDateUploaded(uploadedStr), seeders);
			torrent.setHash(hash);
			torrent.setImdbId(parseImdbUrl(page, title));
			return torrent;
		} catch (Exception e) {
			logService.error(getClass(), "Failed parsing page of search by " + NAME + " torrent id: " + mediaRequest.getSearcherId(NAME) + ". Page:" + page + " Error: " + e.getMessage(), e);
			return null;
		}
	}
}
