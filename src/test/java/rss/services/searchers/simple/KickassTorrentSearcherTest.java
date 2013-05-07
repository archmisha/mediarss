package rss.services.searchers.simple;

import org.junit.Before;
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
import rss.services.requests.SingleEpisodeRequest;
import rss.services.searchers.SearchResult;
import rss.services.shows.ShowService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

/**
 * User: dikmanm
 * Date: 25/01/13 01:35
 */
@RunWith(MockitoJUnitRunner.class)
public class KickAssTorrentSearcherTest extends BaseTest {

	@Mock
	private PageDownloader pageDownloader;

	@Mock
	private ShowService showService;

	@InjectMocks
	private KickAssTorrentSearcher kickAssTorrentSearcher = new KickAssTorrentSearcher();

	@Test
	@SuppressWarnings("unchecked")
	public void testParseSearchResults_singleEpisode() {
		SingleEpisodeRequest episodeRequest = new SingleEpisodeRequest("greys anatomy", new Show(), MediaQuality.HD720P, 1, 1);

		when(pageDownloader.downloadPage(any(String.class))).thenReturn(loadPage("kickass-torrents-search-results-single-episode"));
		Mockito.doAnswer(new Answer<List<ShowService.MatchCandidate>>() {
			@Override
			public List<ShowService.MatchCandidate> answer(InvocationOnMock invocationOnMock) throws Throwable {
				List<ShowService.MatchCandidate> matchCandidates = (List<ShowService.MatchCandidate>) invocationOnMock.getArguments()[1];
				return matchCandidates;
			}
		}).when(showService).filterMatching(any(SingleEpisodeRequest.class), any(List.class));

		SearchResult searchResult = kickAssTorrentSearcher.search(episodeRequest);

		assertEquals(SearchResult.SearchStatus.FOUND, searchResult.getSearchStatus());
		assertEquals(1, searchResult.getTorrents().size());
		assertEquals("Greys Anatomy S01E01 avi", searchResult.getTorrents().get(0).getTitle());
	}
}