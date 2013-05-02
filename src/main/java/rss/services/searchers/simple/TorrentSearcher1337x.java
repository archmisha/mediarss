package rss.services.searchers.simple;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.entities.Media;
import rss.entities.Torrent;
import rss.services.PageDownloader;
import rss.services.requests.MediaRequest;
import rss.services.searchers.SearchResult;
import rss.services.searchers.SimpleTorrentSearcher;
import rss.services.shows.ShowService;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
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
public class TorrentSearcher1337x<T extends MediaRequest, S extends Media> extends SimpleTorrentSearcher<T, S> {

	private static final String HOST_NAME_URL_PART = "1337x.org";
	private static final String SEARCH_URL = "http://" + HOST_NAME_URL_PART + "/search/%s/0/";

	@Autowired
	private PageDownloader pageDownloader;

	private Map<Integer, Pattern> patterns;

	@PostConstruct
	public void postConstruct() {
		patterns = new HashMap<>();
		patterns.put(Calendar.MINUTE, Pattern.compile("(\\d+) minutes?"));
		patterns.put(Calendar.HOUR_OF_DAY, Pattern.compile("(\\d+) hours?"));
		patterns.put(Calendar.DAY_OF_MONTH, Pattern.compile("(\\d+) days?"));
		patterns.put(Calendar.WEEK_OF_MONTH, Pattern.compile("(\\d+) weeks?"));
		patterns.put(Calendar.MONTH, Pattern.compile("(\\d+) months?"));
		patterns.put(Calendar.YEAR, Pattern.compile("(\\d+) years?"));
	}

	@Override
	public SearchResult searchById(T mediaRequest) {
		// todo: handle that case
		return SearchResult.createNotFound();
	}

	@Override
	public String getName() {
		return HOST_NAME_URL_PART;
	}

	@Override
	protected String getSearchUrl(T mediaRequest) throws UnsupportedEncodingException {
		return String.format(SEARCH_URL, URLEncoder.encode(mediaRequest.toQueryString(), "UTF-8"));
	}

	protected SearchResult parseSearchResults(T mediaRequest, String url, String page) {
		List<String> results = new ArrayList<>();

		try {
			int idx = page.indexOf("<div class=\"torrentName\">");
			while (idx > -1) {
				String urlPrefix = "<h3 class=\"org\"><a href=\"";
				idx = page.indexOf(urlPrefix, idx) + urlPrefix.length();
				String torrentUrl = page.substring(idx, page.indexOf("\"", idx));
				results.add(torrentUrl);
				idx = page.indexOf("<div class=\"torrentName\">", idx);
			}
		} catch (Exception e) {
			// in case of an error in parsing, printing the page so be able to reproduce
			logService.error(getClass(), "Failed parsing page of search for: " + mediaRequest.toQueryString() + ". Page:" + page + " Error: " + e.getMessage(), e);
			return SearchResult.createNotFound();
		}

		if (results.isEmpty()) {
			return SearchResult.createNotFound();
		}

		SearchResult searchResult = new SearchResult(getName());
		List<Torrent> torrents = new ArrayList<>();
		for (String result : results) {
			try {
				Pair<Torrent, String> pair = retrieveTorrentEntry(result);
				torrents.add(pair.getKey());

				if (StringUtils.isBlank(searchResult.getMetaData().getImdbUrl())) {
					searchResult.getMetaData().setImdbUrl(pair.getValue());
				}
			} catch (Exception e) {
				logService.error(getClass(), e.getMessage(), e);
				// just skip
			}
		}
		if (torrents.isEmpty()) {
			return SearchResult.createNotFound();
		}


		List<ShowService.MatchCandidate> filteredResults = filterMatchingResults(mediaRequest, torrents);
		if (filteredResults.isEmpty()) {
			return SearchResult.createNotFound();
		}

		//todo: sort the results
		List<ShowService.MatchCandidate> subFilteredResults = filteredResults.subList(0, Math.min(filteredResults.size(), mediaRequest.getResultsLimit()));

		// now for the final results - should download the actual page to get the imdb info if exists
		for (ShowService.MatchCandidate matchCandidate : subFilteredResults) {
			searchResult.addTorrent(matchCandidate.<Torrent>getObject());
		}

		return searchResult;
	}

	private Pair<Torrent, String> retrieveTorrentEntry(String torrentUrl) {
		String page = pageDownloader.downloadPage("http://" + HOST_NAME_URL_PART + torrentUrl);

		int idx = page.indexOf("<div class=\"topHead\">");
		String titlePrefix = "<h2>";
		idx = page.indexOf(titlePrefix, idx) + titlePrefix.length();
		String title = page.substring(idx, page.indexOf("</h2>", idx));
		title = StringEscapeUtils.unescapeHtml4(title);

		String dateUploadedPrefix = "date uploaded</span>";
		idx = page.indexOf(dateUploadedPrefix, idx) + dateUploadedPrefix.length();
		idx = page.indexOf(">", idx) + ">".length();
		String dateUploadedAgoString = page.substring(idx, page.indexOf("</span>", idx));
		Date dateUploadedAgo = parseDateUploaded(dateUploadedAgoString);

		String seedersPrefix = "seeders</span>";
		idx = page.indexOf(seedersPrefix, idx) + seedersPrefix.length();
		idx = page.indexOf(">", idx) + ">".length();
		idx = page.indexOf(">", idx) + ">".length();
		int seeders = Integer.parseInt(page.substring(idx, page.indexOf("</span>", idx)));

		idx = page.indexOf("<div class=\"torrentInfoBtn\">", idx);
		String urlPrefix = "<a href=\"";
		idx = page.indexOf(urlPrefix, idx) + urlPrefix.length();
		String url = page.substring(idx, page.indexOf("\"", idx));

		Torrent torrent = new Torrent(title, url, dateUploadedAgo, seeders);
		String imdbUrl = getImdbUrl(page, title);
		return new ImmutablePair<>(torrent, imdbUrl);
	}

	// 1 week 6 days ago, 17 minutes ago, 1 month ago, 3 years 5 months ago, 2 months 1 week ago, 21 hours 15 minutes ago
	// 1 day 55 minutes ago, 20 hours 28 seconds ago
	// ignoring the seconds part
	private Date parseDateUploaded(String str) {
		// remove the ago suffix
		String suffix = " ago";
		if (str.endsWith(suffix)) {
			str = str.substring(0, str.length() - suffix.length());
		}

		Calendar now = Calendar.getInstance();
		for (Map.Entry<Integer, Pattern> entry : patterns.entrySet()) {
			Matcher matcher = entry.getValue().matcher(str);
			if (matcher.find()) {
				int value = Integer.parseInt(matcher.group(1));
				now.add(entry.getKey(), -value);
			}
		}
		return now.getTime();
	}
}