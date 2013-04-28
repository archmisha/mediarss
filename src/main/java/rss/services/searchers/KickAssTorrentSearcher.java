package rss.services.searchers;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.entities.Media;
import rss.entities.Torrent;
import rss.services.PageDownloader;
import rss.services.searchers.SearchResult;
import rss.services.requests.MediaRequest;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 14:35
 */
@Service("kickAssTorrentSearcher")
public class KickAssTorrentSearcher<T extends MediaRequest, S extends Media> extends AbstractTorrentSearcher<T, S> {

	private static final String HOST_NAME_URL_PART = "kickasstorrents.com";
	private static final String ENTRY_URL = "http://kickasstorrents.com/";
	public static final Pattern IMDB_URL_PATTERN = Pattern.compile("(http://www.imdb.com/title/\\w+)");

	@Autowired
	private PageDownloader pageDownloader;

	@Override
	public String getName() {
		return HOST_NAME_URL_PART;
	}

	@Override
	protected String getSearchUrl(T mediaRequest) throws UnsupportedEncodingException {
		// currently not handling search
		throw new UnsupportedOperationException();
	}

	@Override
	public SearchResult<S> search(T mediaRequest) {
		if (mediaRequest.getKickAssTorrentsId() != null) {
			String page = pageDownloader.downloadPage(ENTRY_URL + mediaRequest.getKickAssTorrentsId());
			return parseTorrentPage(mediaRequest, page);
		} else {
			// currently not handling search
			throw new UnsupportedOperationException();
		}
	}

	protected SearchResult<S> parseSearchResults(T mediaRequest, String url, String page) {
		// currently not handling search
		throw new UnsupportedOperationException();
	}

	private SearchResult<S> parseTorrentPage(T mediaRequest, String page) {
		try {
			String titlePrefix = "<span itemprop=\"name\">";
			int idx = page.indexOf(titlePrefix) + titlePrefix.length();
			String title = page.substring(idx, page.indexOf("</span>", idx)).trim();
			title = StringEscapeUtils.unescapeHtml4(title);

			String seedersPrefix = "<strong itemprop=\"seeders\">";
			idx = page.indexOf(seedersPrefix, idx) + seedersPrefix.length();
			String seedersStr = page.substring(idx, page.indexOf("</strong>", idx));
			int seeders = Integer.parseInt(seedersStr);

			String urlPrefix = "<a title=\"Magnet link\" href=\"";
			idx = page.indexOf(urlPrefix, idx) + urlPrefix.length();
			String torrentUrl = page.substring(idx, page.indexOf("\"", idx));

			String uploadedPrefix = "Added on ";
			idx = page.indexOf(uploadedPrefix, idx) + uploadedPrefix.length();
			String uploadedStr = page.substring(idx, page.indexOf(" in <", idx)).trim();
			// Oct 15, 2006, for some reason without the locale won't parse the month
			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
			Date uploaded = sdf.parse(uploadedStr/*.replaceAll("\\p{Cntrl}", "")*/);

			String hashPrefix = "hash: '";
			idx = page.indexOf(hashPrefix) + hashPrefix.length();
			String hash = page.substring(idx, page.indexOf("'", idx)).trim();

			Torrent torrent = new Torrent(title, torrentUrl, uploaded, seeders, null);
			torrent.setHash(hash);
			SearchResult<S> searchResult = new SearchResult<>(torrent, getName());
			searchResult.getMetaData().setImdbUrl(getImdbUrl(torrent, page));
			return searchResult;
		} catch (Exception e) {
			logService.error(getClass(), "Failed parsing page of search by kickass torrent id: " + mediaRequest.getKickAssTorrentsId() + ". Page:" + page + " Error: " + e.getMessage(), e);
			return SearchResult.createNotFound();
		}
	}

	private String getImdbUrl(Torrent torrent, String page) {
		Matcher matcher = IMDB_URL_PATTERN.matcher(page);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			logNoImdbFound(torrent);
		}
		return null;
	}

	protected void logNoImdbFound(Torrent torrent) {
		logService.info(getClass(), "Didn't find IMDB url for: " + torrent.getTitle());
	}
}