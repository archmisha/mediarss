package rss.services.searchers;

import org.springframework.beans.factory.annotation.Autowired;
import rss.entities.Media;
import rss.entities.Torrent;
import rss.services.PageDownloader;
import rss.services.log.LogService;
import rss.services.requests.MediaRequest;
import rss.services.shows.ShowService;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 19:31
 */
public abstract class SimpleTorrentSearcher<T extends MediaRequest, S extends Media> implements TorrentSearcher<T, S> {

	private static final Pattern IMDB_URL_PATTERN = Pattern.compile("(www.imdb.com/title/\\w+)");

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	protected LogService logService;

	@Autowired
	private ShowService showService;

	@Override
	public SearchResult search(T mediaRequest) {
		String url = null;
		try {
			url = getSearchUrl(mediaRequest);
		} catch (UnsupportedEncodingException e) {
			logService.error(getClass(), "Failed encoding: " + url + " error: " + e.getMessage(), e);
			return SearchResult.createNotFound();
		}

		String page;
		try {
			page = pageDownloader.downloadPage(url);
		} catch (Exception e) {
			logService.error(getClass(), "Page for the url " + url + " could not be retrieved: " + e.getMessage(), e);
			return SearchResult.createNotFound();
		}

		SearchResult searchResult = parseSearchResults(mediaRequest, url, page);
		if (searchResult.getSearchStatus() == SearchResult.SearchStatus.NOT_FOUND) {
			return searchResult;
		}

		// check aging
		// if all torrents are awaigin aging then leave them, but if there is at least one that is not
		// then return it
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		now.add(Calendar.HOUR_OF_DAY, -4);

		Torrent readyTorrent = null;
		for (Torrent torrent : searchResult.getTorrents()) {
			if (!torrent.getDateUploaded().after(now.getTime())) {
				readyTorrent = torrent;
			}
		}

		if (readyTorrent != null) {
			searchResult.getTorrents().clear();
			searchResult.addTorrent(readyTorrent);
			searchResult.setSearchStatus(SearchResult.SearchStatus.FOUND);
			return searchResult;
		}

		searchResult.setSearchStatus(SearchResult.SearchStatus.AWAITING_AGING);
		return searchResult;
	}

	protected String getImdbUrl(String page, String title) {
		Matcher matcher = IMDB_URL_PATTERN.matcher(page);
		if (matcher.find()) {
			return "http://" + matcher.group(1);
		} else {
			logService.debug(getClass(), "Didn't find IMDB url for: " + title);
		}
		return null;
	}

	private ShowService.MatchCandidate toMatchCandidate(final Torrent torrent) {
		return new ShowService.MatchCandidate() {
			@Override
			public String getText() {
				return torrent.getTitle();
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> T getObject() {
				return (T) torrent;
			}
		};
	}

	protected List<ShowService.MatchCandidate> filterMatchingResults(T mediaRequest, List<Torrent> torrents) {
		List<ShowService.MatchCandidate> matchCandidates = new ArrayList<>();
		for (Torrent torrent : torrents) {
			matchCandidates.add(toMatchCandidate(torrent));
		}
		return mediaRequest.visit(new MatcherVisitor(showService), matchCandidates);
	}

	// no need to validate results in here, cuz arrive directly by url, thus no need in a common method i nthe upper level
	public abstract SearchResult searchById(T mediaRequest);

	protected abstract String getSearchUrl(T mediaRequest) throws UnsupportedEncodingException;

	protected abstract SearchResult parseSearchResults(T mediaRequest, String url, String page);
}