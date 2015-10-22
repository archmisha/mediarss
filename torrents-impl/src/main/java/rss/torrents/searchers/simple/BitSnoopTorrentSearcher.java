package rss.torrents.searchers.simple;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import rss.torrents.Torrent;
import rss.torrents.dao.TorrentImpl;
import rss.torrents.requests.MediaRequest;
import rss.torrents.searchers.SearchResult;
import rss.torrents.searchers.SimpleTorrentSearcher;
import rss.util.StringUtils2;

import java.util.ArrayList;
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

	public static final String NAME = "bitsnoop";
	private static final Pattern TORRENTS_ID_PATTERN = Pattern.compile("http://bitsnoop[^/]+/([^\"/]+)");

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getPriority() {
		return -1;
	}

	@Override
	protected Collection<String> getEntryUrl() {
		Collection<String> res = new ArrayList<>();
		for (String domain : searcherConfigurationService.getSearcherConfiguration(getName()).getDomains()) {
			res.add("http://" + domain + "/");
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

	@Override
	protected List<Torrent> parseSearchResultsPage(String url, T mediaRequest, String page) {
		return Collections.emptyList();
	}

	@Override
	public String parseId(MediaRequest mediaRequest, String page) {
		Matcher matcher = TORRENTS_ID_PATTERN.matcher(page);
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

			Torrent torrent = new TorrentImpl(title, torrentUrl, StringUtils2.parseDateUploaded(uploadedStr), seeders);
			torrent.setHash(hash);
			torrent.setImdbId(parseImdbUrl(page, title));
			return torrent;
		} catch (Exception e) {
			logService.error(getClass(), "Failed parsing page of search by " + NAME + " torrent id: " + mediaRequest.getSearcherId(NAME) + ". Page:" + page + " Error: " + e.getMessage(), e);
			return null;
		}
	}
}
