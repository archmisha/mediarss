package rss.services.searchers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import rss.BaseTest;
import rss.entities.Media;
import rss.services.PageDownloader;
import rss.services.searchers.SearchResult;
import rss.services.requests.MovieRequest;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

/**
 * User: dikmanm
 * Date: 25/01/13 01:35
 */
@RunWith(MockitoJUnitRunner.class)
public class ThePirateBayTorrentSearcherTest extends BaseTest {

	@Mock
	private PageDownloader pageDownloader;

	@InjectMocks
	private ThePirateBayTorrentSearcher searcher = new ThePirateBayTorrentSearcher();

	@Test
	public void testSearchWithPirateBayId() {
		String pirateBayId = "123";
		String page = loadPage("piratebay-gangster-squad");
		MovieRequest movieRequest = new MovieRequest("bla", null);
		movieRequest.setPirateBayId(pirateBayId);

		when(pageDownloader.downloadPage(any(String.class))).thenReturn(page);

		SearchResult<Media> searchResult = searcher.search(movieRequest);

		Date dateUploaded = searchResult.getTorrent().getDateUploaded();
		Calendar c = Calendar.getInstance();
		c.setTime(dateUploaded);
		assertEquals(2013, c.get(Calendar.YEAR));
		assertEquals(4, c.get(Calendar.MONTH)+1);
		assertEquals(5, c.get(Calendar.DAY_OF_MONTH));

		assertEquals("http://www.imdb.com/title/tt1321870", searchResult.getMetaData().getImdbUrl());
		assertEquals(SearchResult.SearchStatus.FOUND, searchResult.getSearchStatus());
		assertEquals("Gangster.Squad.2013.720p.BluRay.x264-SPARKS [PublicHD]", searchResult.getTorrent().getTitle());
		assertEquals("A6B3FE8895B278CE06FBCA825AA400A2DBF101A2", searchResult.getTorrent().getHash());
		assertEquals("magnet:?xt=urn:btih:a6b3fe8895b278ce06fbca825aa400a2dbf101a2&dn=Gangster.Squad.2013.720p.BluRay.x264-SPARKS+%5BPublicHD%5D&tr=udp%3A%2F%2Ftracker.openbittorrent.com%3A80&tr=udp%3A%2F%2Ftracker.publicbt.com%3A80&tr=udp%3A%2F%2Ftracker.istole.it%3A6969&tr=udp%3A%2F%2Ftracker.ccc.de%3A80",
				searchResult.getTorrent().getUrl());
		assertEquals(6979, searchResult.getTorrent().getSeeders());
	}

	@Test
	public void testParsePage1() {
		String pirateBayId = "123";
		String page = loadPage("piratebay-broken-city");
		MovieRequest movieRequest = new MovieRequest("bla", null);
		movieRequest.setPirateBayId(pirateBayId);

		when(pageDownloader.downloadPage(any(String.class))).thenReturn(page);

		SearchResult<Media> searchResult = searcher.search(movieRequest);

//		Date dateUploaded = searchResult.getTorrent().getDateUploaded();
//		Calendar c = Calendar.getInstance();
//		c.setTime(dateUploaded);
//		assertEquals(2013, c.get(Calendar.YEAR));
//		assertEquals(4, c.get(Calendar.MONTH)+1);
//		assertEquals(5, c.get(Calendar.DAY_OF_MONTH));

//		assertEquals("http://www.imdb.com/title/tt1321870", searchResult.getMetaData().getImdbUrl());
//		assertEquals(SearchResult.SearchStatus.FOUND, searchResult.getSearchStatus());
//		assertEquals("Gangster.Squad.2013.720p.BluRay.x264-SPARKS [PublicHD]", searchResult.getTorrent().getTitle());
		assertEquals("56F3BA62597048E317757A67406A6155C2B69B33", searchResult.getTorrent().getHash());
//		assertEquals("magnet:?xt=urn:btih:a6b3fe8895b278ce06fbca825aa400a2dbf101a2&dn=Gangster.Squad.2013.720p.BluRay.x264-SPARKS+%5BPublicHD%5D&tr=udp%3A%2F%2Ftracker.openbittorrent.com%3A80&tr=udp%3A%2F%2Ftracker.publicbt.com%3A80&tr=udp%3A%2F%2Ftracker.istole.it%3A6969&tr=udp%3A%2F%2Ftracker.ccc.de%3A80",
//				searchResult.getTorrent().getUrl());
//		assertEquals(6979, searchResult.getTorrent().getSeeders());
	}

	@Test
	public void testParsePage2() {
		String pirateBayId = "123";
		String page = loadPage("piratebay-devils-of-war");
		MovieRequest movieRequest = new MovieRequest("bla", null);
		movieRequest.setPirateBayId(pirateBayId);

		when(pageDownloader.downloadPage(any(String.class))).thenReturn(page);

		SearchResult<Media> searchResult = searcher.search(movieRequest);

		assertEquals("http://www.imdb.com/title/tt2675318", searchResult.getMetaData().getImdbUrl());
	}
}
