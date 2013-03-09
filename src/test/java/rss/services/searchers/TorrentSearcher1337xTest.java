package rss.services.searchers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import rss.BaseTest;
import rss.services.EpisodeRequest;
import rss.entities.Media;
import rss.entities.MediaQuality;
import rss.entities.Show;
import rss.services.PageDownloader;
import rss.services.SearchResult;

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

	@Before
	public void beforeClass() {
		torrentSearcher1337x.postConstruct();
	}

	@Test
	public void testUploadedOn1() {
		String page = loadPage("suits-s01e01-search-results");

		when(pageDownloader.downloadPage(any(String.class))).thenReturn(loadPage("suits-s01e01"));

		SearchResult<Media> searchResult = torrentSearcher1337x.parseSearchResults(new EpisodeRequest("suits", new Show(), MediaQuality.HD720P, 1, 1), "", page);

		Date dateUploaded = searchResult.getTorrent().getDateUploaded();
		Calendar c = Calendar.getInstance();
		c.setTime(dateUploaded);
		assertEquals(2011, c.get(Calendar.YEAR));
		assertEquals(6, c.get(Calendar.MONTH)+1);
	}

	@Test
	public void testImdbUrlParse() {
		String page = loadPage("1337x-taken2-search-results");

		when(pageDownloader.downloadPage(any(String.class))).thenReturn(loadPage("1337x-taken2"));

		SearchResult<Media> searchResult = torrentSearcher1337x.parseSearchResults(new EpisodeRequest("suits", new Show(), MediaQuality.HD720P, 1, 1), "", page);

		assertEquals("http://www.imdb.com/title/tt1397280", searchResult.getMetaData().getImdbUrl());
	}

	@Test
	public void testImdbUrlParse2() {
		String page = loadPage("1337x-rise-of-the-guardians-search-results");

		when(pageDownloader.downloadPage(any(String.class))).thenReturn(loadPage("1337x-rise-of-the-guardians"));

		SearchResult<Media> searchResult = torrentSearcher1337x.parseSearchResults(new EpisodeRequest("suits", new Show(), MediaQuality.HD720P, 1, 1), "", page);

		assertEquals("http://www.imdb.com/title/tt1446192", searchResult.getMetaData().getImdbUrl());
	}
}
