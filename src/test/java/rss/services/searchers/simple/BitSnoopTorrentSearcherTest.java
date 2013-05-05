package rss.services.searchers.simple;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import rss.BaseTest;
import rss.entities.Episode;
import rss.entities.MediaQuality;
import rss.entities.Show;
import rss.services.PageDownloader;
import rss.services.requests.FullSeasonRequest;
import rss.services.requests.ShowRequest;
import rss.services.requests.SingleEpisodeRequest;
import rss.services.searchers.SearchResult;
import rss.services.searchers.SimpleTorrentSearcher;
import rss.services.searchers.composite.CompositeTorrentSearcher;
import rss.services.searchers.composite.EpisodeCompositeSearcher;
import rss.services.searchers.composite.torrentz.EpisodeTorrentzSearcher;
import rss.services.searchers.composite.torrentz.TorrentzParser;
import rss.services.shows.ShowService;

import java.util.Collection;
import java.util.Collections;
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
	private BitSnoopTorrentSearcher<ShowRequest, Episode> bitSnoopTorrentSearcher = new BitSnoopTorrentSearcher<ShowRequest, Episode>();

	@InjectMocks
	@Qualifier("torrentzEpisodeSearcher")
	private CompositeTorrentSearcher<ShowRequest, Episode> torrentzEpisodeSearcher = new EpisodeTorrentzSearcher() {
		@Override
		protected Collection<? extends SimpleTorrentSearcher<ShowRequest, Episode>> getTorrentSearchers() {
			return Collections.singletonList(bitSnoopTorrentSearcher);
		}
	};

	@Test
	@SuppressWarnings("unchecked")
	public void testParseSearchResults_singleEpisode() {
		FullSeasonRequest episodeRequest = new FullSeasonRequest("Survivor", new Show(), MediaQuality.HD720P, 5);

		when(pageDownloader.downloadPage(any(String.class))).thenReturn(loadPage("bitsnoop-survivor-s05-entry"));
		Mockito.doAnswer(new Answer<List<ShowService.MatchCandidate>>() {
			@Override
			public List<ShowService.MatchCandidate> answer(InvocationOnMock invocationOnMock) throws Throwable {
				List<ShowService.MatchCandidate> matchCandidates = (List<ShowService.MatchCandidate>) invocationOnMock.getArguments()[1];
				return matchCandidates;
			}
		}).when(showService).filterMatching(any(SingleEpisodeRequest.class), any(List.class));

		SearchResult searchResult = torrentzEpisodeSearcher.search(episodeRequest);

		assertEquals(SearchResult.SearchStatus.FOUND, searchResult.getSearchStatus());
		assertEquals(1, searchResult.getTorrents().size());
		assertEquals("Survivor - Season 5 - Thailand", searchResult.getTorrents().get(0).getTitle());
	}
}
