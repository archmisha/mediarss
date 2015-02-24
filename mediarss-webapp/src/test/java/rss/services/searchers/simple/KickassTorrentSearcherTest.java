package rss.services.searchers.simple;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import rss.BaseTest;
import rss.PageDownloader;
import rss.entities.MediaQuality;
import rss.entities.Show;
import rss.entities.Torrent;
import rss.services.matching.MatchCandidate;
import rss.services.requests.episodes.SingleEpisodeRequest;
import rss.services.searchers.SearchResult;
import rss.services.shows.ShowService;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

/**
 * User: dikmanm
 * Date: 25/01/13 01:35
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore
public class KickassTorrentSearcherTest extends BaseTest {

	@Mock
	private PageDownloader pageDownloader;

	@Mock
	private ShowService showService;

	@InjectMocks
	private KickAssTorrentSearcher kickAssTorrentSearcher = new KickAssTorrentSearcher();

	@Test
	@SuppressWarnings("unchecked")
	public void testParseSearchResults_singleEpisode() {
		SingleEpisodeRequest episodeRequest = new SingleEpisodeRequest(null, "greys anatomy", new Show(), MediaQuality.HD720P, 1, 1);

		when(pageDownloader.downloadPage(any(String.class))).thenReturn(loadPage("kickass-torrents-search-results-single-episode"));
		Mockito.doAnswer(new Answer<List<MatchCandidate>>() {
			@Override
			public List<MatchCandidate> answer(InvocationOnMock invocationOnMock) throws Throwable {
				return (List<MatchCandidate>) invocationOnMock.getArguments()[1];
			}
		}).when(showService).filterMatching(any(SingleEpisodeRequest.class), any(List.class));

		SearchResult searchResult = kickAssTorrentSearcher.search(episodeRequest);

		assertEquals(SearchResult.SearchStatus.FOUND, searchResult.getSearchStatus());
		assertEquals(1, searchResult.getDownloadables().size());
		assertEquals("Greys Anatomy S01E01 avi", searchResult.<Torrent>getDownloadables().get(0).getTitle());
	}
}
