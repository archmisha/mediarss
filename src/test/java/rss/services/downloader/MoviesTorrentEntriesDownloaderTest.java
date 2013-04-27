package rss.services.downloader;

import rss.BaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.transaction.support.TransactionTemplate;
import rss.dao.MovieDao;
import rss.dao.TorrentDao;
import rss.services.EmailService;
import rss.services.PageDownloader;
import rss.services.PageDownloaderImpl;
import rss.services.SearchResult;
import rss.services.searchers.CompositeMoviesSearcher;
import rss.entities.Media;
import rss.entities.Movie;
import rss.entities.Torrent;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MoviesTorrentEntriesDownloaderTest extends BaseTest {

	@Mock
	private TorrentDao torrentDao;
	@Mock
	private MovieDao movieDao;
	@Mock
	private EmailService emailService;
	@Mock
	private CompositeMoviesSearcher compositeMoviesSearcher;
	@Mock
	private TransactionTemplate transactionTemplate;
	@Mock
	private ExecutorService executor;
	@Mock
	private PageDownloader pageDownloader;

	@InjectMocks
	private MoviesTorrentEntriesDownloader downloader = new MoviesTorrentEntriesDownloader();

	@InjectMocks
	private PageDownloader realPageDownloader = new PageDownloaderImpl();

	@Before
	@Override
	public void setup() {
		super.setup();
		mockExecutorServiceAsDirectExecutor(executor);
		mockTransactionTemplate(transactionTemplate);
	}

	@Test
	public void testStatusNotFound() {
		MovieRequest movieRequest = new MovieRequest("name", "hash");
		Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);

//		when(torrentDao.find(any(Set.class))).thenReturn(Collections.<Torrent>emptyList());
		when(compositeMoviesSearcher.search(movieRequest)).thenReturn(new SearchResult<Media>(SearchResult.SearchStatus.NOT_FOUND));

		DownloadResult<Movie, MovieRequest> downloadResult = downloader.download(movieRequests, executor, false);

		assertEquals(0, downloadResult.getDownloaded().size());
		assertEquals(1, downloadResult.getMissing().size());
	}

	@Test
	public void testStatusAwaitingAging() {
		MovieRequest movieRequest = new MovieRequest("name", "hash");
		Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);
		Torrent torrent = new Torrent("title", "url", new Date(), 5);

//		when(torrentDao.find(any(Set.class))).thenReturn(Collections.<Torrent>emptyList());
		when(compositeMoviesSearcher.search(movieRequest)).thenReturn(new SearchResult<Media>(torrent, "source", SearchResult.SearchStatus.AWAITING_AGING));

		DownloadResult<Movie, MovieRequest> downloadResult = downloader.download(movieRequests, executor, false);

		assertEquals(0, downloadResult.getDownloaded().size());
		assertEquals(0, downloadResult.getMissing().size());
	}

	@Test
	public void testStatusFound() {
		MovieRequest movieRequest = new MovieRequest("name", "hash");
		Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);
		Torrent torrent = new Torrent("title", "url", new Date(), 5);

//		when(torrentDao.find(any(Set.class))).thenReturn(Collections.<Torrent>emptyList());
		when(compositeMoviesSearcher.search(movieRequest)).thenReturn(new SearchResult<Media>(torrent, "source", SearchResult.SearchStatus.FOUND));

		DownloadResult<Movie, MovieRequest> downloadResult = downloader.download(movieRequests, executor, false);

		assertEquals(1, downloadResult.getDownloaded().size());
		assertEquals(0, downloadResult.getMissing().size());
	}

	@Test
	public void testStatusFoundButOldYearInImdb() {
		int curYear = Calendar.getInstance().get(Calendar.YEAR);
		final MovieRequest movieRequest = new MovieRequest("Batman 1966 " + curYear, "hash"); // putting both current year and old year on same
		Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);
		Torrent torrent = new Torrent("title", "url", new Date(), 5);
		SearchResult<Media> searchResult = new SearchResult<>(torrent, "source", SearchResult.SearchStatus.FOUND);
		String imdbUrl = "some imdb url";
		searchResult.getMetaData().setImdbUrl(imdbUrl);

		// using MatcherResult implementation cuz otherwise Mockito screws up the Matcher object
		when(pageDownloader.downloadPageUntilFound(eq(imdbUrl), any(Pattern.class)))
				.thenReturn("<meta name=\"title\" content=\"" + movieRequest.getTitle() + " (1966) - IMDb\" /><span itemprop=\"ratingCount\">1001</span>");
//		when(torrentDao.find(any(Set.class))).thenReturn(Collections.<Torrent>emptyList());
		when(compositeMoviesSearcher.search(movieRequest)).thenReturn(searchResult);

		DownloadResult<Movie, MovieRequest> downloadResult = downloader.download(movieRequests, executor, false);

		assertEquals(0, downloadResult.getDownloaded().size());
		assertEquals(0, downloadResult.getMissing().size());
	}

	@Test
	public void testFindInCache() {
		int curYear = Calendar.getInstance().get(Calendar.YEAR);
		final MovieRequest movieRequest = new MovieRequest("Batman 1966 " + curYear, "hash"); // putting both current year and old year on same
		Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);
		Torrent torrent = new Torrent("title", "url", new Date(), 5);
		SearchResult<Media> searchResult = new SearchResult<>(torrent, "source", SearchResult.SearchStatus.FOUND);
		String imdbUrl = "some imdb url";
		searchResult.getMetaData().setImdbUrl(imdbUrl);

		// using MatcherResult implementation cuz otherwise Mockito screws up the Matcher object
		when(pageDownloader.downloadPageUntilFound(eq(imdbUrl), any(Pattern.class)))
				.thenReturn("<meta name=\"title\" content=\"" + movieRequest.getTitle() + " (1966) - IMDb\" /><span itemprop=\"ratingCount\">1001</span>");
//		when(torrentDao.find(any(Set.class))).thenReturn(Collections.<Torrent>emptyList());
		when(compositeMoviesSearcher.search(movieRequest)).thenReturn(searchResult);

		DownloadResult<Movie, MovieRequest> downloadResult = downloader.download(movieRequests, executor, false);

		assertEquals(0, downloadResult.getDownloaded().size());
		assertEquals(0, downloadResult.getMissing().size());
	}

	@Test
	public void testImdbTitleEscaping() {
		final MovieRequest movieRequest = new MovieRequest("name", "hash");
		Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);
		Torrent torrent = new Torrent("title", "url", new Date(), 5);
		SearchResult<Media> searchResult = new SearchResult<>(torrent, "source", SearchResult.SearchStatus.FOUND);
		String imdbUrl = "http://www.imdb.com/title/tt2193021/";
		searchResult.getMetaData().setImdbUrl(imdbUrl);

		// using MatcherResult implementation cuz otherwise Mockito screws up the Matcher object
		when(pageDownloader.downloadPageUntilFound(eq(imdbUrl), any(Pattern.class))).then(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocationOnMock) throws Throwable {
				return loadPage("imdb-arrow");
			}
		});
//		when(torrentDao.find(any(Set.class))).thenReturn(Collections.<Torrent>emptyList());
		when(compositeMoviesSearcher.search(movieRequest)).thenReturn(searchResult);

		DownloadResult<Movie, MovieRequest> downloadResult = downloader.download(movieRequests, executor, false);

		assertEquals(1, downloadResult.getDownloaded().size());
		assertEquals(0, downloadResult.getMissing().size());
		Movie movie = downloadResult.getDownloaded().iterator().next();
		System.out.println(movie.getName());
		assertEquals("Arrow (TV Series 2012– )", movie.getName());
	}


	@Test
	public void testImdbHighViewersNumber_movieOk() {
		final MovieRequest movieRequest = new MovieRequest("name", "hash");
		Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);
		Torrent torrent = new Torrent("title", "url", new Date(), 5);
		SearchResult<Media> searchResult = new SearchResult<>(torrent, "source", SearchResult.SearchStatus.FOUND);
		String imdbUrl = "http://www.imdb.com/title/tt2402582/";
		searchResult.getMetaData().setImdbUrl(imdbUrl);

		// using MatcherResult implementation cuz otherwise Mockito screws up the Matcher object
		when(pageDownloader.downloadPageUntilFound(eq(imdbUrl), any(Pattern.class))).then(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocationOnMock) throws Throwable {
				return loadPage("imdb-argo");
			}
		});
//		when(torrentDao.find(any(Set.class))).thenReturn(Collections.<Torrent>emptyList());
		when(compositeMoviesSearcher.search(movieRequest)).thenReturn(searchResult);

		DownloadResult<Movie, MovieRequest> downloadResult = downloader.download(movieRequests, executor, false);

		assertEquals(1, downloadResult.getDownloaded().size());
		assertEquals(0, downloadResult.getMissing().size());
	}

	@Test
	public void testImdbLowViewersNumber_rejectMovie() {
		final MovieRequest movieRequest = new MovieRequest("name", "hash");
		Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);
		Torrent torrent = new Torrent("title", "url", new Date(), 5);
		SearchResult<Media> searchResult = new SearchResult<>(torrent, "source", SearchResult.SearchStatus.FOUND);
		String imdbUrl = "http://www.imdb.com/title/tt2402582/";
		searchResult.getMetaData().setImdbUrl(imdbUrl);

		// using MatcherResult implementation cuz otherwise Mockito screws up the Matcher object
		when(pageDownloader.downloadPageUntilFound(eq(imdbUrl), any(Pattern.class))).then(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocationOnMock) throws Throwable {
				return loadPage("imdb-low-users");
			}
		});
//		when(torrentDao.find(any(Set.class))).thenReturn(Collections.<Torrent>emptyList());
		when(compositeMoviesSearcher.search(movieRequest)).thenReturn(searchResult);

		DownloadResult<Movie, MovieRequest> downloadResult = downloader.download(movieRequests, executor, false);

		assertEquals(0, downloadResult.getDownloaded().size());
		assertEquals(0, downloadResult.getMissing().size());
	}

	@Test
	public void testComingSoonMovie_rejectMovie() {
		final MovieRequest movieRequest = new MovieRequest("name", "hash");
		Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);
		Torrent torrent = new Torrent("title", "url", new Date(), 5);
		SearchResult<Media> searchResult = new SearchResult<>(torrent, "source", SearchResult.SearchStatus.FOUND);
		String imdbUrl = "url";
		searchResult.getMetaData().setImdbUrl(imdbUrl);

		// using MatcherResult implementation cuz otherwise Mockito screws up the Matcher object
		when(pageDownloader.downloadPageUntilFound(eq(imdbUrl), any(Pattern.class))).then(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocationOnMock) throws Throwable {
				return loadPage("future-movie-no-viewers");
			}
		});
//		when(torrentDao.find(any(Set.class))).thenReturn(Collections.<Torrent>emptyList());
		when(compositeMoviesSearcher.search(movieRequest)).thenReturn(searchResult);

		DownloadResult<Movie, MovieRequest> downloadResult = downloader.download(movieRequests, executor, false);

		assertEquals(0, downloadResult.getDownloaded().size());
		assertEquals(0, downloadResult.getMissing().size());
	}
}