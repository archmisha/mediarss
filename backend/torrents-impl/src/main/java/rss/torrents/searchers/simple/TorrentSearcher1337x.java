package rss.torrents.searchers.simple;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.PageDownloadException;
import rss.PageDownloader;
import rss.RecoverableConnectionException;
import rss.torrents.Torrent;
import rss.torrents.dao.TorrentImpl;
import rss.torrents.requests.MediaRequest;
import rss.torrents.searchers.SimpleTorrentSearcher;
import rss.util.StringUtils2;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 14:35
 * <p/>
 * <div class="torrentName">
 * <a href="/sub/41/0/" class="TV" title="TV > HD"></a>
 * <h3 class="org"><a href="/torrent/429465/How-I-Met-Your-Mother-S08E07-720p-HDTV-X264-DIMENSION/" class="org">How I Met Your Mother S08E07 720p HDTV X264-DIMENSION</a></h3>
 * <div class="clr"></div>
 * </div>
 * <span class="seed">982</span>
 */
@Service("episodeSearcher1337x")
public class TorrentSearcher1337x<T extends MediaRequest> extends SimpleTorrentSearcher<T> {

	private static final String NAME = "1337x";
	private static final String SEARCH_URL_SUFFIX = "search/%s/0/";

	private static final Pattern TORRENTS_ID_PATTERN = Pattern.compile("http://1337x[^/]+/([^\"]+)");

	@Autowired
	private PageDownloader pageDownloader;

//	@Override
//	protected String getSearchByIdUrl(T mediaRequest) {
//		// todo: handle that case
//		return null;
//	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDefaultDomain() {
		return "http://1337x.net/";
	}

	@Override
	protected Collection<String> getEntryUrl() {
		return searcherConfigurationService.getSearcherConfiguration(getName()).getDomains();
	}

	@Override
	public String parseId(MediaRequest mediaRequest, String page) {
		Matcher matcher = TORRENTS_ID_PATTERN.matcher(page);
		if (matcher.find()) {
			String group = matcher.group(1);
			if (group.equals("torrent")) {
				System.out.println("ds");
			}
			return group;
		}
		return null;
	}

	@Override
	protected Collection<String> getSearchUrl(T mediaRequest) throws UnsupportedEncodingException {
		Collection<String> res = new ArrayList<>();
		for (String domain : searcherConfigurationService.getSearcherConfiguration(getName()).getDomains()) {
			res.add(String.format(domain + SEARCH_URL_SUFFIX, URLEncoder.encode(mediaRequest.toQueryString(), "UTF-8")));
		}
		return res;

//		return Collections.singletonList(String.format(SEARCH_URL, URLEncoder.encode(mediaRequest.toQueryString(), "UTF-8")));
	}

	@Override
	// 1337x search results page lacks magnet links and age, so must download the torrent entry page always
	protected List<Torrent> parseSearchResultsPage(String url, T mediaRequest, String page) {
		List<Torrent> results = new ArrayList<>();
		Document doc = Jsoup.parse(page);
		for (Element element : doc.select(".torrentName")) {
			for (String domain : searcherConfigurationService.getSearcherConfiguration(getName()).getDomains()) {
				try {
					// take second a tag
					Element a = element.select("a.org").get(0);
					String searcherTorrentsId = a.attr("href");
					if (searcherTorrentsId.startsWith("/")) {
						searcherTorrentsId = searcherTorrentsId.substring(1, searcherTorrentsId.length());
					}
					String torrentUrl = domain + searcherTorrentsId;
					mediaRequest.setSearcherId(NAME, torrentUrl);
					String entryPage = pageDownloader.downloadPage(torrentUrl);
					Torrent torrent = parseTorrentPage(mediaRequest, entryPage);
					results.add(torrent);
					// if any of the domains worked no point continuing
					break;
				} catch (RecoverableConnectionException e) {
					// don't want to send email of 'Connection timeout out' errors, cuz tvrage is slow sometimes
					// will retry to update show status in the next job run - warn level not send to email
					logService.warn(getClass(), "Failed parsing page of search for: " + mediaRequest.toQueryString() + ". Error: " + e.getMessage());
				} catch (PageDownloadException e) {
					logService.error(getClass(), "Failed parsing page of search for: " + mediaRequest.toQueryString() + ". Page:" + page + " Error: " + e.getMessage());
				} catch (Exception e) {
					// in case of an error in parsing, printing the page so be able to reproduce
					logService.error(getClass(), "Failed parsing page of search for: " + mediaRequest.toQueryString() + ". Page:" + page + " Error: " + e.getMessage(), e);
				}
			}
		}
		return results;
	}

	@Override
	protected Torrent parseTorrentPage(T mediaRequest, String page) {
		try {
			int idx = page.indexOf("<div class=\"top-row");
			String titlePrefix = "<strong>";
			idx = page.indexOf(titlePrefix, idx) + titlePrefix.length();
			String title = page.substring(idx, page.indexOf("</strong>", idx));
			title = StringEscapeUtils.unescapeHtml4(title);

			String sizePrefix = "Total size</strong>";
			idx = page.indexOf(sizePrefix, idx) + sizePrefix.length();
			idx = page.indexOf(">", idx) + ">".length();
			String[] arr = page.substring(idx, page.indexOf("</span>", idx)).split(" ");
			double size = Double.parseDouble(arr[0]);
			if (arr[1].equals("GB")) {
				size *= 1024;
			}

			String dateUploadedPrefix = "Date uploaded</strong>";
			idx = page.indexOf(dateUploadedPrefix, idx) + dateUploadedPrefix.length();
			idx = page.indexOf(">", idx) + ">".length();
			String dateUploadedAgoString = page.substring(idx, page.indexOf("</span>", idx));
			Date dateUploadedAgo = StringUtils2.parseDateUploaded(dateUploadedAgoString);

			String seedersPrefix = "Seeders</strong>";
			idx = page.indexOf(seedersPrefix, idx) + seedersPrefix.length();
			idx = page.indexOf(">", idx) + ">".length();
			int seeders = Integer.parseInt(page.substring(idx, page.indexOf("</span>", idx)));

			idx = page.indexOf("<ul class=\"download-links\">", idx);
			String urlPrefix = "<a href=\"";
			idx = page.indexOf(urlPrefix, idx) + urlPrefix.length();
			String url = page.substring(idx, page.indexOf("\"", idx));

			Torrent torrent = new TorrentImpl(title, url, dateUploadedAgo, seeders);
			torrent.setSize((int) size);
			torrent.setImdbId(parseImdbUrl(page, title));
			return torrent;
		} catch (Exception e) {
			logService.error(getClass(), "Failed parsing page of search by " + NAME + " torrent id: " + mediaRequest.getSearcherId(NAME) + ". Page:" + page + " Error: " + e.getMessage(), e);
			return null;
		}
	}
}