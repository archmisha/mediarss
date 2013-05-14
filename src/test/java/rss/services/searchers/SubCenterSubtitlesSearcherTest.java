package rss.services.searchers;

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
import rss.entities.Show;
import rss.entities.Torrent;
import rss.services.PageDownloader;
import rss.services.matching.MatchCandidate;
import rss.services.requests.SingleEpisodeRequest;
import rss.services.requests.SubtitlesSingleEpisodeRequest;
import rss.services.shows.ShowService;

import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * User: dikmanm
 * Date: 11/05/13 20:52
 */
@RunWith(MockitoJUnitRunner.class)
public class SubCenterSubtitlesSearcherTest extends BaseTest {

	@Mock
	private PageDownloader pageDownloader;

	@Mock
	ShowService showService;

	@InjectMocks
	private SubCenterSubtitlesSearcher subCenterSubtitlesSearcher = new SubCenterSubtitlesSearcher();

	@SuppressWarnings("unchecked")
	@Test
	public void testSearchResultParse() {
		when(pageDownloader.downloadPage(any(String.class)))
				.thenReturn(loadPage("subcenter-search-results-house"))
				.thenReturn(loadPage("subcenter-search-results-house-page2"))
				.thenReturn(loadPage("subcenter-search-results-house-page3"))
				.thenReturn(loadPage("subcenter-search-results-house-page4"))
				.thenReturn(loadPage("subcenter-search-results-house-page5"))
				.thenReturn(loadPage("subcenter-search-results-house-page6"))
				.thenReturn(loadPage("subcenter-search-results-house-page7"));
		Mockito.doAnswer(new Answer<List<MatchCandidate>>() {
			@Override
			public List<MatchCandidate> answer(InvocationOnMock invocationOnMock) throws Throwable {
				return (List<MatchCandidate>) invocationOnMock.getArguments()[1];
			}
		}).when(showService).filterMatching(any(SingleEpisodeRequest.class), any(List.class));

		Show show = new Show("House");
		Torrent torrent = new Torrent("House", "A", new Date(), 1, "a");
		Episode episode = new Episode(1, 1);
		episode.setShow(show);

		SearchResult searchResult = subCenterSubtitlesSearcher.search(new SubtitlesSingleEpisodeRequest(torrent, show, 1, 1));
	}
}
