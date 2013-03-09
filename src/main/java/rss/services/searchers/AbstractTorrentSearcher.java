package rss.services.searchers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import rss.services.PageDownloader;
import rss.services.MediaRequest;
import rss.entities.Media;
import rss.services.SearchResult;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 19:31
 */
public abstract class AbstractTorrentSearcher implements TorrentSearcher<MediaRequest, Media> {

    private static Log log = LogFactory.getLog(AbstractTorrentSearcher.class);

	@Autowired
	private PageDownloader pageDownloader;

    @Override
    public SearchResult<Media> search(MediaRequest mediaRequest) {
        String url = null;
        try {
            url = String.format(getSearchUrl(), URLEncoder.encode(mediaRequest.toQueryString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("Failed encoding: " + url + " error: " + e.getMessage(), e);
            return new SearchResult<>(SearchResult.SearchStatus.NOT_FOUND);
        }

		String page;
		try {
			page = pageDownloader.downloadPage(url);
		} catch (Exception e) {
			log.error("Page for the url " + url + " could not be retrieved: " + e.getMessage(), e);
			return new SearchResult<>(SearchResult.SearchStatus.NOT_FOUND);
		}

        SearchResult<Media> searchResult = parseSearchResults(mediaRequest, url, page);
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

    protected abstract String getSearchUrl();

    protected abstract SearchResult<Media> parseSearchResults(MediaRequest mediaRequest, String url, String page);
}