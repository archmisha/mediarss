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
import rss.util.StringUtils2;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	private static final String ENTRY_URL = "http://" + HOST_NAME_URL_PART;

	@Autowired
	private PageDownloader pageDownloader;

	@Override
	protected String getSearchByIdUrl(T mediaRequest) {
		// todo: handle that case
		return null;
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

	@Override
	protected String getImdbUrl(Torrent torrent) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	protected List<Torrent> parseSearchResultsPage(T mediaRequest, String page) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	protected SearchResult parseTorrentPage(T mediaRequest, String page) {
		return null;
	}

	private Pair<Torrent, String> retrieveTorrentEntry(String torrentUrl) {
		String page = pageDownloader.downloadPage(ENTRY_URL + torrentUrl);

		int idx = page.indexOf("<div class=\"topHead\">");
		String titlePrefix = "<h2>";
		idx = page.indexOf(titlePrefix, idx) + titlePrefix.length();
		String title = page.substring(idx, page.indexOf("</h2>", idx));
		title = StringEscapeUtils.unescapeHtml4(title);

		String dateUploadedPrefix = "date uploaded</span>";
		idx = page.indexOf(dateUploadedPrefix, idx) + dateUploadedPrefix.length();
		idx = page.indexOf(">", idx) + ">".length();
		String dateUploadedAgoString = page.substring(idx, page.indexOf("</span>", idx));
		Date dateUploadedAgo = StringUtils2.parseDateUploaded(dateUploadedAgoString);

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
		String imdbUrl = parseImdbUrl(page, title);
		return new ImmutablePair<>(torrent, imdbUrl);
	}
}