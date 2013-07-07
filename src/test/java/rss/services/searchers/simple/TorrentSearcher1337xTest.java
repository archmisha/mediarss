package rss.services.searchers.simple;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import rss.BaseTest;
import rss.entities.MediaQuality;
import rss.entities.Show;
import rss.services.PageDownloader;
import rss.services.matching.MatchCandidate;
import rss.services.requests.episodes.SingleEpisodeRequest;
import rss.services.searchers.SearchResult;
import rss.services.shows.ShowService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

/**
 * User: dikmanm
 * Date: 25/01/13 01:35
 */
@RunWith(MockitoJUnitRunner.class)
public class TorrentSearcher1337xTest extends BaseTest {

	@Mock
	private PageDownloader pageDownloader;

	@Mock
	private ShowService showService;

	@InjectMocks
	private TorrentSearcher1337x torrentSearcher1337x = new TorrentSearcher1337x();

	@Test
	public void testUploadedOn1() {
		when(pageDownloader.downloadPage(any(String.class))).thenReturn(loadPage("suits-s01e01-search-results")).thenReturn(loadPage("suits-s01e01"));
		mockFilterReturnAll();

		SearchResult searchResult = torrentSearcher1337x.search(new SingleEpisodeRequest(null, "suits", new Show(), MediaQuality.HD720P, 1, 1));

		Calendar supposedTobe = Calendar.getInstance();
		supposedTobe.add(Calendar.YEAR, -1);
		supposedTobe.add(Calendar.MONTH, -7);

		Date dateUploaded = searchResult.getDownloadables().get(0).getDateUploaded();
		Calendar c = Calendar.getInstance();
		c.setTime(dateUploaded);
		assertEquals(supposedTobe.get(Calendar.YEAR), c.get(Calendar.YEAR));
		assertEquals(supposedTobe.get(Calendar.MONTH) + 1, c.get(Calendar.MONTH) + 1);
	}

	@Test
	public void testImdbUrlParse() {
		when(pageDownloader.downloadPage(any(String.class))).thenReturn(loadPage("1337x-taken2-search-results")).thenReturn(loadPage("1337x-taken2"));
		mockFilterReturnAll();

		SearchResult searchResult = torrentSearcher1337x.search(new SingleEpisodeRequest(null, "suits", new Show(), MediaQuality.HD720P, 1, 1));

//		assertEquals("http://www.imdb.com/title/tt1397280", searchResult.getImdbId());
	}

	@Test
	public void testImdbUrlParse2() {
		when(pageDownloader.downloadPage(any(String.class))).thenReturn(loadPage("1337x-rise-of-the-guardians-search-results")).thenReturn(loadPage("1337x-rise-of-the-guardians"));
		mockFilterReturnAll();

		SearchResult searchResult = torrentSearcher1337x.search(new SingleEpisodeRequest(null, "suits", new Show(), MediaQuality.HD720P, 1, 1));

//		assertEquals("http://www.imdb.com/title/tt1446192", searchResult.getImdbId());
	}

	@Test
	public void testNoResults() {
		when(pageDownloader.downloadPage(any(String.class))).thenReturn(loadPage("1337x-no-results"));

		SearchResult searchResult = torrentSearcher1337x.search(new SingleEpisodeRequest(null, "suits", new Show(), MediaQuality.HD720P, 1, 1));
		mockFilterReturnAll();

		assertEquals(SearchResult.SearchStatus.NOT_FOUND, searchResult.getSearchStatus());
	}

	private void mockFilterReturnAll() {
		Mockito.doAnswer(new Answer<List<MatchCandidate>>() {
			@Override
			public List<MatchCandidate> answer(InvocationOnMock invocationOnMock) throws Throwable {
				List<MatchCandidate> matchCandidates = (List<MatchCandidate>) invocationOnMock.getArguments()[1];
				return matchCandidates;
			}
		}).when(showService).filterMatching(any(SingleEpisodeRequest.class), any(List.class));
	}

}
