package rss.torrents.searchers.simple;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import rss.PageDownloader;
import rss.PageDownloaderTestUtils;
import rss.shows.ShowService;
import rss.torrents.MediaQuality;
import rss.torrents.Show;
import rss.torrents.Torrent;
import rss.torrents.matching.MatchCandidate;
import rss.torrents.requests.shows.FullSeasonRequest;
import rss.torrents.requests.shows.ShowRequest;
import rss.torrents.requests.shows.SingleEpisodeRequest;
import rss.torrents.searchers.SearchResult;
import rss.torrents.searchers.TorrentzParser;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * User: dikmanm
 * Date: 25/01/13 01:35
 */
@RunWith(MockitoJUnitRunner.class)
public class BitSnoopTorrentSearcherTest {

	@Mock
	private PageDownloader pageDownloader;

	@Mock
	private ShowService showService;

	@Mock
	protected TorrentzParser torrentzParser;

	@InjectMocks
	private BitSnoopTorrentSearcher<ShowRequest> bitSnoopTorrentSearcher = new BitSnoopTorrentSearcher<>();

	@Test
    @Ignore
	@SuppressWarnings("unchecked")
	public void testParseSearchResults_singleEpisode() {
		Show show = mock(Show.class);
		FullSeasonRequest episodeRequest = new FullSeasonRequest(null, "Survivor", show, MediaQuality.HD720P, 5);

		when(pageDownloader.downloadPage(any(String.class))).thenReturn(PageDownloaderTestUtils.loadPage("bitsnoop-survivor-s05-entry"));
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
