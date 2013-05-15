package rss.services.searchers.composite.torrentz;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import rss.BaseTest;
import rss.services.PageDownloader;
import rss.services.requests.movies.MovieRequest;
import rss.services.searchers.SearchResult;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * User: dikmanm
 * Date: 11/05/13 14:15
 */
@RunWith(MockitoJUnitRunner.class)
public class MovieTorrentzSearcherTest extends BaseTest {

	@Mock
	private PageDownloader pageDownloader;

	@Mock
	private TorrentzParser torrentzParser;

	@InjectMocks
	private TorrentzParser realTorrentzParser = new TorrentzParserImpl();

	@InjectMocks
	private MovieTorrentzSearcher movieTorrentzSearcher = new MovieTorrentzSearcher();

	@Test
	@Ignore("not finished writing")
	public void testMatchingSearchResults() {
		String page = loadPage("torrentz-2fast2furious-search-results");
		MovieRequest movieRequest = new MovieRequest("2 Fast 2 Furious (2003) keltz", null);

		when(pageDownloader.downloadPage(any(String.class))).thenReturn(page);
		Set<TorrentzResult> torrentzResults = realTorrentzParser.downloadByUrl("bla");
		when(torrentzParser.downloadByUrl(any(String.class))).thenReturn(torrentzResults);

		SearchResult searchResult = movieTorrentzSearcher.search(movieRequest);

		assertEquals(SearchResult.SearchStatus.FOUND, searchResult.getSearchStatus());
	}

}
