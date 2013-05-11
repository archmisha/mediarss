package rss.services.searchers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import rss.entities.Media;
import rss.entities.Torrent;
import rss.services.PageDownloader;
import rss.services.log.LogService;
import rss.services.requests.MediaRequest;
import rss.services.requests.MovieRequest;
import rss.services.shows.ShowService;
import rss.util.Utils;

import java.io.UnsupportedEncodingException;
import java.util.*;
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
			if (Utils.isRootCauseMessageContains(e, "404 Not Found")) {
				logService.debug(getClass(), "Page for the url " + url + " could not be retrieved: " + e.getMessage());
			} else {
				logService.error(getClass(), "Page for the url " + url + " could not be retrieved: " + e.getMessage(), e);
			}
			return SearchResult.createNotFound();
		}

		SearchResult searchResult = parseSearchResults(mediaRequest, page);
		if (searchResult.getSearchStatus() == SearchResult.SearchStatus.NOT_FOUND) {
			return searchResult;
		}

		// check aging
		// if all torrents are awaiting aging then leave them, but if there is at least one that is not then return it
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		now.add(Calendar.HOUR_OF_DAY, -4);

		List<Torrent> readyTorrents = new ArrayList<>();
		for (Torrent torrent : searchResult.getTorrents()) {
			if (!torrent.getDateUploaded().after(now.getTime())) {
				readyTorrents.add(torrent);
			}
		}

		if (!readyTorrents.isEmpty()) {
			searchResult.getTorrents().clear();
			searchResult.getTorrents().addAll(readyTorrents);
			searchResult.setSearchStatus(SearchResult.SearchStatus.FOUND);
			return searchResult;
		}

		searchResult.setSearchStatus(SearchResult.SearchStatus.AWAITING_AGING);
		return searchResult;
	}

	// no need to validate results in here, cuz arrive directly by url, thus no need in a common method in the upper level
	public SearchResult searchById(T mediaRequest) {
		String url = this.getSearchByIdUrl(mediaRequest);
		if (url == null) {
			return SearchResult.createNotFound();
		}

		String page = pageDownloader.downloadPage(url);
		Torrent torrent = parseTorrentPage(mediaRequest, page);
		if (torrent == null) {
			return SearchResult.createNotFound();
		}

		SearchResult searchResult = new SearchResult(getName());
		searchResult.addTorrent(torrent);
		return searchResult;
	}

	private SearchResult parseSearchResults(T mediaRequest, String page) {
		List<Torrent> torrents = parseSearchResultsPage(mediaRequest, page);
		if (torrents.isEmpty()) {
			return SearchResult.createNotFound();
		}

		List<ShowService.MatchCandidate> filteredResults = filterMatchingResults(mediaRequest, torrents);
		if (filteredResults.isEmpty()) {
			return SearchResult.createNotFound();
		}

		if (filteredResults.size() > 1) {
			sortResults(filteredResults);
		}

		List<ShowService.MatchCandidate> subFilteredResults = filteredResults.subList(0, Math.min(filteredResults.size(), mediaRequest.getResultsLimit()));

		SearchResult searchResult = new SearchResult(getName());
		for (ShowService.MatchCandidate matchCandidate : subFilteredResults) {
			searchResult.addTorrent(matchCandidate.<Torrent>getObject());
		}

		// now for the final results - should download the actual page to get the imdb info if exists
		if (mediaRequest instanceof MovieRequest) {
			for (Torrent torrent : searchResult.getTorrents()) {
				if (StringUtils.isBlank(searchResult.getImdbId())) {
					torrent.setImdbId(getImdbUrl(torrent));
				}
			}
		}

		return searchResult;
	}

	protected abstract String getSearchUrl(T mediaRequest) throws UnsupportedEncodingException;

	protected String getSearchByIdUrl(T mediaRequest) {
		String searcherId = mediaRequest.getSearcherId(getName());
		if (searcherId != null) {
			return getEntryUrl() + searcherId;
		}
		return null;
	}

	protected abstract String getEntryUrl();

	protected abstract List<Torrent> parseSearchResultsPage(T mediaRequest, String page);

	protected abstract Torrent parseTorrentPage(T mediaRequest, String page);

	public abstract String parseId(MediaRequest mediaRequest, String page);

	// might be in the headers of the torrent or in the content as plain text
	private String getImdbUrl(Torrent torrent) {
		String page;
		try {
			page = pageDownloader.downloadPage(torrent.getSourcePageUrl());
		} catch (Exception e) {
			logService.error(getClass(), "Failed retrieving the imdb url of " + torrent.toString() + ": " + e.getMessage(), e);
			return null;
		}

		return parseImdbUrl(page, torrent.getTitle());
	}

	protected String parseImdbUrl(String page, String title) {
		Matcher matcher = IMDB_URL_PATTERN.matcher(page);
		if (matcher.find()) {
			return "http://" + matcher.group(1);
		} else {
			logService.debug(getClass(), "Didn't find IMDB url for: " + title);
		}
		return null;
	}

	// reverse sort by seeders
	protected void sortResults(List<ShowService.MatchCandidate> filteredResults) {
		Collections.sort(filteredResults, new Comparator<ShowService.MatchCandidate>() {
			@Override
			public int compare(ShowService.MatchCandidate o1, ShowService.MatchCandidate o2) {
				Torrent torrent1 = o1.getObject();
				Torrent torrent2 = o2.getObject();
				return new Integer(torrent2.getSeeders()).compareTo(torrent1.getSeeders());
			}
		});
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
}