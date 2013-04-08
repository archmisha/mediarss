package rss.services.movies;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import rss.BaseTest;
import rss.entities.Movie;
import rss.services.PageDownloader;
import rss.services.downloader.DownloadResult;
import rss.services.downloader.MovieRequest;
import rss.services.downloader.MoviesTorrentEntriesDownloader;
import rss.services.parsers.PageParser;
import rss.services.parsers.TorrentzParser;

import java.util.Collection;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

/**
 * User: dikmanm
 * Date: 30/12/12 22:42
 */
@RunWith(MockitoJUnitRunner.class)
public class TorrentzServiceImplTest extends BaseTest {

	@Mock
	private PageDownloader pageDownloader;

	@Mock
	private PageParser torrentzParser;

	@Mock
	private MoviesTorrentEntriesDownloader moviesTorrentEntriesDownloader;

	@InjectMocks
	private TorrentzService torrentzService = new TorrentzServiceImpl();

	@Test
	public void testDownloadShowList() {
		String name = "For the Love of Money 2012 1080P DTS DD 5 1 Ccustom nl subs NLtoppers";
		String searchResultsPage = loadPage("torrentz-for-the-love-of-money-search-results");
		String entryPage = loadPage("torrentz-for-the-love-of-money-entry");
		doReturn(searchResultsPage).doReturn(entryPage).when(pageDownloader).downloadPage(anyString());
		TorrentzParser realTorrentzParser = new TorrentzParser();
		Set<MovieRequest> parsedPage = realTorrentzParser.parse(searchResultsPage);
		doReturn(parsedPage).when(torrentzParser).parse(anyString());
		doReturn(realTorrentzParser.getPirateBayId(entryPage)).when(torrentzParser).getPirateBayId(anyString());

		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				Collection<MovieRequest> movieRequests = (Collection<MovieRequest>) invocationOnMock.getArguments()[0];
				Assert.assertFalse(movieRequests.isEmpty());
				MovieRequest movieRequest = movieRequests.iterator().next();
				Assert.assertNotNull("piratebay id is not null", movieRequest.getPirateBayId());
				return null;
			}
		}).when(moviesTorrentEntriesDownloader).download(any(Collection.class));

		Movie movie = new Movie(name, null);
		DownloadResult<Movie, MovieRequest> downloadResult = torrentzService.downloadMovie(movie);
//		Assert.assertTrue(downloadResult.getMissing().isEmpty());
	}
}
