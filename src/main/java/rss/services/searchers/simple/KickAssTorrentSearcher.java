package rss.services.searchers.simple;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import rss.entities.Media;
import rss.entities.Torrent;
import rss.services.requests.*;
import rss.services.searchers.SimpleTorrentSearcher;
import rss.util.StringUtils2;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 14:35
 */
@Service("kickAssTorrentSearcher")
public class KickAssTorrentSearcher<T extends MediaRequest, S extends Media> extends SimpleTorrentSearcher<T, S> {

	public static final String NAME = "kat.ph";
	private static final String ENTRY_URL = "http://" + NAME + "/";
	private static final String SEARCH_URL = "http://" + NAME + "/usearch/";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected String getSearchUrl(T mediaRequest) throws UnsupportedEncodingException {
		// todo: need some kind of visitor here
		if (mediaRequest instanceof EpisodeRequest) {
			StringBuilder sb = new StringBuilder();
			sb.append("season:").append(((EpisodeRequest) mediaRequest).getSeason());
			if (mediaRequest instanceof SingleEpisodeRequest) {
				sb.append(" episode:").append(((SingleEpisodeRequest) mediaRequest).getEpisode());
			} else if (mediaRequest instanceof FullSeasonRequest) {
				// its ok
			} else {
				return SEARCH_URL + URLEncoder.encode(mediaRequest.toQueryString() + " category:tv " + sb.toString(), "UTF-8");
			}
			//"greys anatomy category:tv season:1 episode:1"
			return SEARCH_URL + URLEncoder.encode(mediaRequest.toQueryString() + " category:tv " + sb.toString(), "UTF-8");
		} else if (mediaRequest instanceof MovieRequest) {
			// http://kat.ph/usearch/iron%20man%20category:movies/
			return SEARCH_URL + URLEncoder.encode(mediaRequest.toQueryString() + " category:movies", "UTF-8");
		} else {
			throw new IllegalArgumentException(mediaRequest.getClass() + " is not supported");
		}
	}

	@Override
	protected String getSearchByIdUrl(T mediaRequest) {
		if (mediaRequest.getSearcherId(NAME) != null) {
			return ENTRY_URL + mediaRequest.getSearcherId(NAME);
		}
		return null;
	}

	@Override
	protected List<Torrent> parseSearchResultsPage(T mediaRequest, String page) {
		List<Torrent> results = new ArrayList<>();
		try {
			Document doc = Jsoup.parse(page);
			for (Element element : doc.select(".odd,.even")) {
				// take second a tag
				Element a = element.select(".torrentname a").get(1);
				String kickAssTorrentsId = a.attr("href");
				String title = StringEscapeUtils.unescapeHtml4(a.text());
				String magnetLink = element.select(".imagnet").attr("href");
				// text for some reason did problems with the &nbsp; conversion
				String age = element.select("td.center").get(2).html().replaceAll("&nbsp;", " ");
				int seeders = Integer.parseInt(element.select("td.green.center").text());

				Torrent torrent = new Torrent(title, magnetLink, StringUtils2.parseDateUploaded(age), seeders, ENTRY_URL + kickAssTorrentsId);
				results.add(torrent);
			}
		} catch (Exception e) {
			// in case of an error in parsing, printing the page so be able to reproduce
			logService.error(getClass(), "Failed parsing page of search for: " + mediaRequest.toQueryString() + ". Page:" + page + " Error: " + e.getMessage(), e);
			return Collections.emptyList(); // could maybe return the partial results collected up until now
		}
		return results;
	}

	@Override
	protected Torrent parseTorrentPage(T mediaRequest, String page) {
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

			Torrent torrent = new Torrent(title, torrentUrl, uploaded, seeders);
			torrent.setHash(hash);
			torrent.setImdbId(parseImdbUrl(page, title));
			return torrent;
		} catch (Exception e) {
			logService.error(getClass(), "Failed parsing page of search by " + NAME + " torrent id: " + mediaRequest.getSearcherId(NAME) + ". Page:" + page + " Error: " + e.getMessage(), e);
			return null;
		}
	}
}