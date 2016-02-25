package rss.torrents.searchers.composite.torrentz;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import rss.PageDownloader;
import rss.PageDownloaderTestUtils;
import rss.torrents.requests.movies.MovieRequest;
import rss.torrents.searchers.SearchResult;
import rss.torrents.searchers.TorrentzParser;
import rss.torrents.searchers.TorrentzResult;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * User: dikmanm
 * Date: 11/05/13 14:15
 */
@RunWith(MockitoJUnitRunner.class)
public class MovieTorrentzSearcherTest {

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
        String page = PageDownloaderTestUtils.loadPage("torrentz-2fast2furious-search-results");
        MovieRequest movieRequest = new MovieRequest("2 Fast 2 Furious (2003) keltz", null);

        when(pageDownloader.downloadPage(any(String.class))).thenReturn(page);
        Collection<TorrentzResult> torrentzResults = realTorrentzParser.downloadByUrl("bla");
        when(torrentzParser.downloadByUrl(any(String.class))).thenReturn(torrentzResults);

        SearchResult searchResult = movieTorrentzSearcher.search(movieRequest);

        assertEquals(SearchResult.SearchStatus.FOUND, searchResult.getSearchStatus());
    }

}
