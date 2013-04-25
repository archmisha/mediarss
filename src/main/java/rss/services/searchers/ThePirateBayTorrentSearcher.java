package rss.services.searchers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.entities.Media;
import rss.entities.Torrent;
import rss.services.log.LogService;
import rss.services.requests.MediaRequest;
import rss.services.PageDownloader;
import rss.services.SearchResult;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 19:21
 */
@Service("thePirateBayTorrentSearcher")
public class ThePirateBayTorrentSearcher<T extends MediaRequest, S extends Media> extends AbstractTorrentSearcher<T, S> {

	private static final String NAME = "thepiratebay.se";
	private static final String HOST_NAME_URL_PART = "http://" + NAME;
	private static final String SEARCH_URL = HOST_NAME_URL_PART + "/search/%s/0/99/0";
	private static final String ENTRY_URL = HOST_NAME_URL_PART + "/torrent/";
	public static final Pattern IMDB_URL_PATTERN = Pattern.compile("(http://www.imdb.com/title/\\w+)");

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	protected LogService logService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected String getSearchUrl(T mediaRequest) throws UnsupportedEncodingException {
		return String.format(SEARCH_URL, URLEncoder.encode(mediaRequest.toQueryString(), "UTF-8"));
	}

	@Override
	public SearchResult<S> search(T mediaRequest) {
		if (mediaRequest.getPirateBayId() != null) {
			String page = pageDownloader.downloadPage(ENTRY_URL + mediaRequest.getPirateBayId());
			return parseTorrentPage(mediaRequest, page);
		} else {
			return super.search(mediaRequest);
		}
	}

	private SearchResult<S> parseTorrentPage(T mediaRequest, String page) {
		try {
			String titlePrefix = "<div id=\"title\">";
			int idx = page.indexOf(titlePrefix);
			String title = page.substring(idx + titlePrefix.length(), page.indexOf("</div>", idx)).trim();

			String urlPrefix = "<div class=\"download\">";
			idx = page.indexOf(urlPrefix);
			String urlPrefix2 = "href=\"";
			int idx2 = page.indexOf(urlPrefix2, idx) + urlPrefix2.length();
			String torrentUrl = page.substring(idx2, page.indexOf("\"", idx2));

			idx = page.indexOf("<dt>Uploaded:</dt>");
			String uploadedPrefix = "<dd>";
			idx = page.indexOf(uploadedPrefix, idx);
			idx2 = idx + uploadedPrefix.length();
			String uploadedStr = page.substring(idx2, page.indexOf("</dd>", idx2));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
			Date uploaded = sdf.parse(uploadedStr);

			idx = page.indexOf("<dt>Seeders:</dt>");
			String seedersPrefix = "<dd>";
			idx = page.indexOf(seedersPrefix, idx);
			idx2 = idx + seedersPrefix.length();
			String seedersStr = page.substring(idx2, page.indexOf("</dd>", idx2));
			int seeders = Integer.parseInt(seedersStr);

			String hashPrefix = "<dt>Info Hash:</dt><dd>&nbsp;</dd>";
			idx = page.indexOf(hashPrefix);
			idx2 = idx + hashPrefix.length();
			String hash = page.substring(idx2, page.indexOf("</dl>", idx2)).trim();

			Torrent torrent = new Torrent(title, torrentUrl, uploaded, seeders, null);
			torrent.setHash(hash);
			SearchResult<S> searchResult = new SearchResult<>(torrent, getName());
			searchResult.getMetaData().setImdbUrl(getImdbUrl(torrent, page));
			return searchResult;
		} catch (Exception e) {
			logService.error(getClass(), "Failed parsing page of search by piratebay id: " + mediaRequest.getPirateBayId() + ". Page:" + page + " Error: " + e.getMessage(), e);
			return new SearchResult<>(SearchResult.SearchStatus.NOT_FOUND);
		}
	}

	protected SearchResult<S> parseSearchResults(T mediaRequest, String url, String page) {
		List<Torrent> results = parseSearchResultsPage(mediaRequest, page);

		if (results.isEmpty()) {
			//			log.info("There were no search results for: " + tvShowEpisode.toQueryString() + " url=" + url);
			return new SearchResult<>(SearchResult.SearchStatus.NOT_FOUND);
		}

		verifySearchResults(mediaRequest, results);
		if (results.isEmpty()) {
			return new SearchResult<>(SearchResult.SearchStatus.NOT_FOUND);
		}

		if (results.size() > 1) {
			sortResults(results);
		}

		Torrent torrent = results.get(0);
		SearchResult<S> searchResult = new SearchResult<>(torrent, getName());

		// now for the final result - should download the actual page to get the imdb info if exists
		searchResult.getMetaData().setImdbUrl(getImdbUrl(torrent));

		return searchResult;
	}

	protected void verifySearchResults(T mediaRequest, List<Torrent> results) {
		for (Torrent torrent : new ArrayList<>(results)) {
			if (!torrent.getTitle().toLowerCase().contains(mediaRequest.getTitle().toLowerCase())) {
				results.remove(torrent);
				logService.info(getClass(), "Removing '" + torrent.getTitle() + "' cuz a bad match for '" + mediaRequest.toString() + "'");
			}
		}
	}

	// might be in the headers of the torrent or in the content as plain text
	private String getImdbUrl(Torrent torrent) {
		String page;
		try {
			page = pageDownloader.downloadPage(torrent.getSourcePageUrl());
		} catch (Exception e) {
			logService.error(getClass(), "Failed retrieving the imdb url of " + torrent.toString() + ": " + e.getMessage(), e);
			return null;
		}

		return getImdbUrl(torrent, page);
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

	private List<Torrent> parseSearchResultsPage(MediaRequest mediaRequest, String page) {
		List<Torrent> results = new ArrayList<>();
		try {
			int idx = page.indexOf("<td class=\"vertTh\">");
			while (idx > -1) {
				idx = page.indexOf("<div class=\"detName\">", idx);
				String urlPrefix = "<a href=\"";
				idx = page.indexOf(urlPrefix, idx) + urlPrefix.length();
				String sourcePageUrl = HOST_NAME_URL_PART + page.substring(idx, page.indexOf("\"", idx));
				int i = sourcePageUrl.lastIndexOf("/") + 1;
				sourcePageUrl = sourcePageUrl.substring(0, i) + URLEncoder.encode(sourcePageUrl.substring(i), "UTF-8");

				idx = page.indexOf(">", idx) + ">".length();
				String title = page.substring(idx, page.indexOf("</a>", idx));

				idx = page.indexOf(urlPrefix, idx) + urlPrefix.length();
				String torrentUrl = page.substring(idx, page.indexOf("\"", idx));

				String dateUploadedPrefix = "Uploaded ";
				idx = page.indexOf(dateUploadedPrefix, idx) + dateUploadedPrefix.length();
				String tmp = page.substring(idx, page.indexOf(",", idx));
				Date dateUploaded = parseDateUploaded(tmp);

				String seedersPrefix = "<td align=\"right\">";
				idx = page.indexOf(seedersPrefix, idx) + seedersPrefix.length();
				int seeders = Integer.parseInt(page.substring(idx, page.indexOf("</td>", idx)));

				Torrent torrent = new Torrent(title, torrentUrl, dateUploaded, seeders, sourcePageUrl);
				results.add(torrent);

				idx = page.indexOf("<td class=\"vertTh\">", idx);
			}
		} catch (Exception e) {
			// in case of an error in parsing, printing the page so be able to reproduce
			logService.error(getClass(), "Failed parsing page of search for: " + mediaRequest.toQueryString() + ". Page:" + page + " Error: " + e.getMessage(), e);
			return Collections.emptyList(); // could maybe return the partial results collected up until now
		}

		// sometimes search actually produces no results, so don't want to print that as an error
//        if ( results.isEmpty()) {
//            log.error("Failed parsing page of search for: " + tvShowEpisode.toQueryString() + ". Page:" + page);
//        }

		return results;
	}

	private void sortResults(List<Torrent> results) {
		// sort by seeders
		Collections.sort(results, new Comparator<Torrent>() {
			@Override
			public int compare(Torrent o1, Torrent o2) {
				return new Integer(o1.getSeeders()).compareTo(o2.getSeeders());
			}
		});

		// if first result is ahead by more than 500 seeders then its good
		if (results.get(0).getSeeders() - results.get(1).getSeeders() > 500) {
			return;
		}

		// sorting the urls to get the better results first and stuff like swesubs at the end
		// /torrent/7837683/The.Big.Bang.Theory.S06E08.720p.SWESUB
		// /torrent/7830137/The.Big.Bang.Theory.S06E08.720p.HDTV.X264-DIMENSION_[PublicHD]
		Collections.sort(results, new Comparator<Torrent>() {
			@Override
			public int compare(Torrent o1, Torrent o2) {
				Integer n1 = rateUrl(o1);
				Integer n2 = rateUrl(o2);

				if (n1.equals(n2)) {
					// more seeders the better
					n1 = Integer.MAX_VALUE - o1.getSeeders();
					n2 = Integer.MAX_VALUE - o2.getSeeders();
				}

				return n1.compareTo(n2);
			}

			private Integer rateUrl(Torrent torrent) {
				String url = torrent.getUrl();
				if (url.contains("DIMENSION") && url.contains("eztv")) {
					return 1;
				} else if (url.contains("DIMENSION") && url.contains("PublicHD")) {
					return 2;
				} else if (url.contains("DIMENSION")) {
					return 3;
				} else if (url.contains("eztv")) {
					return 4;
				} else if (url.contains("PublicHD")) {
					return 5;
				}
				return 6;
			}
		});
	}

	private Date parseDateUploaded(String dateUploadedStr) {
		Calendar c = Calendar.getInstance();
		if (dateUploadedStr.startsWith("<b>")) {
			// cases: Uploaded <b>51&nbsp;mins&nbsp;ago</b>
			dateUploadedStr = dateUploadedStr.substring("<b>".length(), dateUploadedStr.length() - "</b>".length());
			String[] arr = dateUploadedStr.split("&nbsp;");
			int mainsSubtract = Integer.parseInt(arr[0]);
			c.add(Calendar.MINUTE, -mainsSubtract);
		} else {
			// cases: Uploaded Today&nbsp;21:46, Uploaded Y-day&nbsp;23:11, Uploaded 01-14&nbsp;2010, Uploaded 11-16&nbsp;01:41
			String[] arr = dateUploadedStr.split("&nbsp;");
			if (arr[0].contains("Today")) {
				// don't set month and day
			} else if (arr[0].contains("Y-day")) {
				c.add(Calendar.DAY_OF_MONTH, -1);
			} else {
				String[] arr2 = arr[0].split("-");
				c.set(Calendar.MONTH, Integer.parseInt(arr2[0]) - 1);
				c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(arr2[1]));
			}

			if (arr[1].contains(":")) {
				String[] arr3 = arr[1].split(":");
				c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(arr3[0]));
				c.set(Calendar.MINUTE, Integer.parseInt(arr3[1]));
			} else { // its a year, not a time
				c.set(Calendar.YEAR, Integer.parseInt(arr[1]));
			}
		}

		Date time = c.getTime();
		// fix pirate bay bug, that shows today when actually its yesterday
		if (time.after(new Date())) {
			logService.debug(getClass(), "Fixing pirate bay date bug for " + dateUploadedStr);
			c.add(Calendar.DAY_OF_MONTH, -1);
		}

		return c.getTime();
	}
}
