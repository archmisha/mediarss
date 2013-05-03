package rss.services.searchers.simple;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import rss.BaseTest;
import rss.entities.MediaQuality;
import rss.entities.Show;
import rss.services.PageDownloader;
import rss.services.searchers.SearchResult;
import rss.services.requests.SingleEpisodeRequest;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * User: dikmanm
 * Date: 25/01/13 01:35
 */
@RunWith(MockitoJUnitRunner.class)
public class TorrentSearcher1337xTest  extends BaseTest {

	@Mock
	private PageDownloader pageDownloader;

	@InjectMocks
	private TorrentSearcher1337x torrentSearcher1337x = new TorrentSearcher1337x();

	@Test
	public void testUploadedOn1() {
		String page = loadPage("suits-s01e01-search-results");

		when(pageDownloader.downloadPage(any(String.class))).thenReturn(loadPage("suits-s01e01"));

		SearchResult searchResult = torrentSearcher1337x.parseSearchResults(new SingleEpisodeRequest("suits", new Show(), MediaQuality.HD720P, 1, 1), "", page);

		Date dateUploaded = searchResult.getTorrents().get(0).getDateUploaded();
		Calendar c = Calendar.getInstance();
		c.setTime(dateUploaded);
		assertEquals(2011, c.get(Calendar.YEAR));
		assertEquals(8, c.get(Calendar.MONTH)+1);
	}

	@Test
	public void testImdbUrlParse() {
		String page = loadPage("1337x-taken2-search-results");

		when(pageDownloader.downloadPage(any(String.class))).thenReturn(loadPage("1337x-taken2"));

		SearchResult searchResult = torrentSearcher1337x.parseSearchResults(new SingleEpisodeRequest("suits", new Show(), MediaQuality.HD720P, 1, 1), "", page);

		assertEquals("http://www.imdb.com/title/tt1397280", searchResult.getMetaData().getImdbUrl());
	}

	@Test
	public void testImdbUrlParse2() {
		String page = loadPage("1337x-rise-of-the-guardians-search-results");

		when(pageDownloader.downloadPage(any(String.class))).thenReturn(loadPage("1337x-rise-of-the-guardians"));

		SearchResult searchResult = torrentSearcher1337x.parseSearchResults(new SingleEpisodeRequest("suits", new Show(), MediaQuality.HD720P, 1, 1), "", page);

		assertEquals("http://www.imdb.com/title/tt1446192", searchResult.getMetaData().getImdbUrl());
	}

	@Test
	public void testNoResults() {
		String page = loadPage("1337x-no-results");

		SearchResult searchResult = torrentSearcher1337x.parseSearchResults(new SingleEpisodeRequest("suits", new Show(), MediaQuality.HD720P, 1, 1), "", page);

		assertEquals(SearchResult.SearchStatus.NOT_FOUND, searchResult.getSearchStatus());
	}
}
