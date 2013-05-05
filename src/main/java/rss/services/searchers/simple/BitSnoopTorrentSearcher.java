package rss.services.searchers.simple;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import rss.entities.Media;
import rss.entities.Torrent;
import rss.services.requests.MediaRequest;
import rss.services.searchers.SearchResult;
import rss.services.searchers.SimpleTorrentSearcher;
import rss.util.StringUtils2;

import java.util.Collections;
import java.util.List;

/**
 * User: dikmanm
 * Date: 04/05/13 16:40
 */
@Service("bitSnoopTorrentSearcher")
public class BitSnoopTorrentSearcher<T extends MediaRequest, S extends Media> extends SimpleTorrentSearcher<T, S> {

	public static final String NAME = "bitsnoop.com";
	private static final String ENTRY_URL = "http://" + NAME + "/";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected String getSearchUrl(T mediaRequest) {
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
	protected String getSearchByIdUrl(T mediaRequest) {
		if (mediaRequest.getSearcherId(NAME) != null) {
			return ENTRY_URL + mediaRequest.getSearcherId(NAME);
		}
		return null;
	}

	@Override
	protected Torrent parseTorrentPage(T mediaRequest, String page) {
		try {
			Document doc = Jsoup.parse(page);
			String title = doc.select("#torrentHeader h1").get(0).text();
			String torrentUrl = doc.select("a[title=Magnet Link]").get(0).attr("href");
			int seeders = Integer.parseInt(doc.select("#torrentHeader .seeders").html());
			String[] arr = doc.select("#details li").get(0).html().split(" ");
			String hash = arr[arr.length-1];
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
