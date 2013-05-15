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
import rss.entities.Episode;
import rss.entities.MediaQuality;
import rss.entities.Show;
import rss.entities.Torrent;
import rss.services.PageDownloader;
import rss.services.matching.MatchCandidate;
import rss.services.requests.episodes.FullSeasonRequest;
import rss.services.requests.episodes.ShowRequest;
import rss.services.requests.episodes.SingleEpisodeRequest;
import rss.services.searchers.SearchResult;
import rss.services.searchers.composite.torrentz.TorrentzParser;
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
public class BitSnoopTorrentSearcherTest extends BaseTest {

	@Mock
	private PageDownloader pageDownloader;

	@Mock
	private ShowService showService;

	@Mock
	protected TorrentzParser torrentzParser;

	@InjectMocks
	private BitSnoopTorrentSearcher<ShowRequest, Episode> bitSnoopTorrentSearcher = new BitSnoopTorrentSearcher<>();

	@Test
	@SuppressWarnings("unchecked")
	public void testParseSearchResults_singleEpisode() {
		FullSeasonRequest episodeRequest = new FullSeasonRequest("Survivor", new Show(), MediaQuality.HD720P, 5);

		when(pageDownloader.downloadPage(any(String.class))).thenReturn(loadPage("bitsnoop-survivor-s05-entry"));
		Mockito.doAnswer(new Answer<List<MatchCandidate>>() {
			@Override
			public List<MatchCandidate> answer(InvocationOnMock invocationOnMock) throws Throwable {
				List<MatchCandidate> matchCandidates = (List<MatchCandidate>) invocationOnMock.getArguments()[1];
				return matchCandidates;
			}
		}).when(showService).filterMatching(any(SingleEpisodeRequest.class), any(List.class));

		SearchResult searchResult = bitSnoopTorrentSearcher.search(episodeRequest);

		assertEquals(SearchResult.SearchStatus.FOUND, searchResult.getSearchStatus());
		assertEquals(1, searchResult.getDownloadables().size());
		assertEquals("Survivor - Season 5 - Thailand", searchResult.<Torrent>getDownloadables().get(0).getTitle());
	}
}
