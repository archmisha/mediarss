package rss.services.searchers;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import rss.ConnectionTimedOutException;
import rss.PageDownloadException;
import rss.entities.Torrent;
import rss.services.PageDownloader;
import rss.services.log.LogService;
import rss.services.matching.MatchCandidate;
import rss.services.matching.MatcherVisitor;
import rss.services.requests.MediaRequest;
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
public abstract class SimpleTorrentSearcher<T extends MediaRequest> implements Searcher<T> {

	private static final Pattern IMDB_URL_PATTERN = Pattern.compile("(www.imdb.com/title/\\w+)");

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	protected LogService logService;

	@Autowired
	private ShowService showService;

	@Autowired
	protected SearcherConfigurationService searcherConfigurationService;

	@Override
	public SearchResult search(T mediaRequest) {
		Collection<String> searchUrls;
		try {
			searchUrls = getSearchUrl(mediaRequest);
		} catch (Exception e) {
			logService.error(getClass(), "Failed searching for: " + mediaRequest + ". Error: " + e.getMessage(), e);
			return SearchResult.createNotFound();
		}

		for (String url : searchUrls) {
			try {
				String page = pageDownloader.downloadPage(url);

				SearchResult searchResult = parseSearchResults(url, mediaRequest, page);
				if (searchResult.getSearchStatus() == SearchResult.SearchStatus.NOT_FOUND) {
					return searchResult;
				}

				// verify torrent name
				for (Torrent torrent : new ArrayList<>(searchResult.<Torrent>getDownloadables())) {
					if (torrent.getTitle().contains(".exe")) {
						logService.info(getClass(), "== Removing torrent because title contains '.exe' !!! (" + torrent + "'");
						searchResult.getDownloadables().remove(torrent);
					}
				}

				// check aging
				// if all torrents are awaiting aging then leave them, but if there is at least one that is not then return it
				Calendar now = Calendar.getInstance();
				now.setTime(new Date());
				now.add(Calendar.HOUR_OF_DAY, -4);

				List<Torrent> readyTorrents = new ArrayList<>();
				for (Torrent torrent : searchResult.<Torrent>getDownloadables()) {
					if (!torrent.getDateUploaded().after(now.getTime())) {
						readyTorrents.add(torrent);
					}
				}

				if (!readyTorrents.isEmpty()) {
					searchResult.getDownloadables().clear();
					searchResult.getDownloadables().addAll(readyTorrents);
					searchResult.setSearchStatus(SearchResult.SearchStatus.FOUND);
					return searchResult;
				}

				searchResult.setSearchStatus(SearchResult.SearchStatus.AWAITING_AGING);
				return searchResult;
			} catch (ConnectionTimedOutException e) {
				// don't want to send email of 'Connection timeout out' errors, cuz tvrage is slow sometimes
				// will retry to update show status in the next job run - warn level not send to email
				logService.warn(getClass(), String.format("Failed retrieving \"%s\": %s", mediaRequest, e.getMessage()));

			} catch (PageDownloadException e) {
				logService.error(getClass(), String.format("Failed retrieving \"%s\": %s", mediaRequest, e.getMessage()));
			} catch (Exception e) {
				if (Utils.isRootCauseMessageContains(e, "404 Not Found")) {
					logService.debug(getClass(), "Page for the url " + url + " could not be retrieved: " + e.getMessage());
				} else {
					logService.error(getClass(), "Page for the url " + url + " could not be retrieved: " + e.getMessage(), e);
				}
			}
		}

		return SearchResult.createNotFound();
	}

	// no need to validate results in here, cuz arrive directly by url, thus no need in a common method in the upper level
	public SearchResult searchById(T mediaRequest) {
		String searcherId = mediaRequest.getSearcherId(getName());
		if (searcherId == null) {
			return SearchResult.createNotFound();
		}

		for (String entryUrl : this.getEntryUrl()) {
			try {
				String url = entryUrl + searcherId;
				String page = pageDownloader.downloadPage(url);
				Torrent torrent = parseTorrentPage(mediaRequest, page);
				if (torrent != null) {
					SearchResult searchResult = new SearchResult(getName());
					searchResult.addDownloadable(torrent);
					return searchResult;
				}
			} catch (PageDownloadException e) {
				logService.error(getClass(), "Request: " + mediaRequest + " Error: " + e.getMessage());
			}
		}

		return SearchResult.createNotFound();
	}

	private SearchResult parseSearchResults(String url, T mediaRequest, String page) {
		List<Torrent> torrents = parseSearchResultsPage(url, mediaRequest, page);
		if (torrents.isEmpty()) {
			return SearchResult.createNotFound();
		}

		List<MatchCandidate> filteredResults = filterMatchingResults(mediaRequest, torrents);
		if (filteredResults.isEmpty()) {
			return SearchResult.createNotFound();
		}

		if (filteredResults.size() > 1) {
			sortResults(filteredResults);
		}

		List<MatchCandidate> subFilteredResults = filteredResults.subList(0, Math.min(filteredResults.size(), mediaRequest.getResultsLimit()));

		SearchResult searchResult = new SearchResult(getName());
		for (MatchCandidate matchCandidate : subFilteredResults) {
			searchResult.addDownloadable(matchCandidate.<Torrent>getObject());
		}

		mediaRequest.visit(new PostParseSearchResultsVisitor(), new ImmutablePair<SimpleTorrentSearcher, SearchResult>(this, searchResult));

		return searchResult;
	}

	protected abstract Collection<String> getSearchUrl(T mediaRequest) throws UnsupportedEncodingException;

	public int getPriority() {
		return 10;
	}

	protected abstract Collection<String> getEntryUrl();

	protected abstract List<Torrent> parseSearchResultsPage(String url, T mediaRequest, String page);

	protected abstract Torrent parseTorrentPage(T mediaRequest, String page);

	public abstract String parseId(MediaRequest mediaRequest, String page);

	// might be in the headers of the torrent or in the content as plain text
	/*package*/ String getImdbUrl(Torrent torrent) {
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
	protected void sortResults(List<MatchCandidate> filteredResults) {
		Collections.sort(filteredResults, new Comparator<MatchCandidate>() {
			@Override
			public int compare(MatchCandidate o1, MatchCandidate o2) {
				Torrent torrent1 = o1.getObject();
				Torrent torrent2 = o2.getObject();
				return new Integer(torrent2.getSeeders()).compareTo(torrent1.getSeeders());
			}
		});
	}

	private MatchCandidate toMatchCandidate(final Torrent torrent) {
		return new MatchCandidate() {
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

	protected List<MatchCandidate> filterMatchingResults(T mediaRequest, List<Torrent> torrents) {
		List<MatchCandidate> matchCandidates = new ArrayList<>();
		for (Torrent torrent : torrents) {
			matchCandidates.add(toMatchCandidate(torrent));
		}
		return mediaRequest.visit(new MatcherVisitor(showService), matchCandidates);
	}
}