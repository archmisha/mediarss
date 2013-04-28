package rss.services.searchers;

import org.springframework.beans.factory.annotation.Autowired;
import rss.entities.Media;
import rss.services.PageDownloader;
import rss.services.searchers.SearchResult;
import rss.services.log.LogService;
import rss.services.requests.MediaRequest;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 19:31
 */
public abstract class AbstractTorrentSearcher<T extends MediaRequest, S extends Media> implements TorrentSearcher<T, S> {

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	protected LogService logService;

	@Override
	public SearchResult<S> search(T mediaRequest) {
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

		SearchResult<S> searchResult = parseSearchResults(mediaRequest, url, page);
		if (searchResult.getSearchStatus() == SearchResult.SearchStatus.NOT_FOUND) {
			return searchResult;
		}

		// check aging
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		now.add(Calendar.HOUR_OF_DAY, -4);
		if (searchResult.getTorrent().getDateUploaded().after(now.getTime())) {
			searchResult.setSearchStatus(SearchResult.SearchStatus.AWAITING_AGING);
		}

		return searchResult;
	}

	protected abstract String getSearchUrl(T mediaRequest) throws UnsupportedEncodingException;

	protected abstract SearchResult<S> parseSearchResults(T mediaRequest, String url, String page);
}