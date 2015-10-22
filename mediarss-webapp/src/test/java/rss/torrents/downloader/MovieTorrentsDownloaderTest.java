//package rss.torrents.downloader;
//
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.invocation.InvocationOnMock;
//import org.mockito.runners.MockitoJUnitRunner;
//import org.mockito.stubbing.Answer;
//import org.springframework.transaction.support.TransactionTemplate;
//import rss.BaseTest;
//import rss.PageDownloader;
//import rss.PageDownloaderImpl;
//import rss.movies.dao.MovieDao;
//import rss.torrents.TorrentDao;
//import rss.torrents.Movie;
//import rss.mail.EmailService;
//import rss.torrents.requests.movies.MovieRequest;
//import rss.torrents.searchers.SearchResult;
//import rss.torrents.searchers.composite.MoviesCompositeSearcher;
//import rss.torrents.Torrent;
//
//import java.util.Calendar;
//import java.util.Collections;
//import java.util.Date;
//import java.util.Set;
//import java.util.concurrent.ExecutorService;
//import java.util.regex.Pattern;
//
//import static org.junit.Assert.assertEquals;
//import static org.mockito.Mockito.*;
//
//@RunWith(MockitoJUnitRunner.class)
//@Ignore
//public class MovieTorrentsDownloaderTest extends BaseTest {
//
//	@Mock
//	private TorrentDao torrentDao;
//	@Mock
//	private MovieDao movieDao;
//	@Mock
//	private EmailService emailService;
//	@Mock
//	private MoviesCompositeSearcher compositeMoviesSearcher;
//	@Mock
//	private TransactionTemplate transactionTemplate;
//	@Mock
//	private ExecutorService executor;
//	@Mock
//	private PageDownloader pageDownloader;
//
//	@InjectMocks
//	private MovieTorrentsDownloaderImpl downloader = new MovieTorrentsDownloaderImpl();
//
//	@InjectMocks
//	private PageDownloader realPageDownloader = new PageDownloaderImpl();
//
//	@Before
//	@Override
//	public void setup() {
//		super.setup();
//		mockExecutorServiceAsDirectExecutor(executor);
//		mockTransactionTemplate(transactionTemplate);
//	}
//
//	@Test
//	public void testStatusNotFound() {
//		MovieRequest movieRequest = new MovieRequest("name", "hash");
//		Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);
//
////		when(torrentDao.find(any(Set.class))).thenReturn(Collections.<Torrent>emptyList());
//		when(compositeMoviesSearcher.search(movieRequest)).thenReturn(SearchResult.createNotFound());
//
//		DownloadConfig downloadConfig = new DownloadConfig();
//		downloadConfig.setForceDownload(false);
//		DownloadResult<Movie, MovieRequest> downloadResult = downloader.download(movieRequests, executor, downloadConfig);
//
//		assertEquals(0, downloadResult.getDownloaded().size());
//		assertEquals(1, downloadResult.getMissing().size());
//	}
//
//	@Test
//	public void testStatusAwaitingAging() {
//		MovieRequest movieRequest = new MovieRequest("name", "hash");
//		Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);
//		Torrent torrent = new Torrent("title", "url", new Date(), 5);
//
//		SearchResult searchResult = new SearchResult("source");
//		searchResult.setSearchStatus(SearchResult.SearchStatus.AWAITING_AGING);
//		searchResult.addDownloadable(torrent);
//
////		when(torrentDao.find(any(Set.class))).thenReturn(Collections.<Torrent>emptyList());
//		when(compositeMoviesSearcher.search(movieRequest)).thenReturn(searchResult);
//
//		DownloadConfig downloadConfig = new DownloadConfig();
//		downloadConfig.setForceDownload(false);
//		DownloadResult<Movie, MovieRequest> downloadResult = downloader.download(movieRequests, executor, downloadConfig);
//
//		assertEquals(0, downloadResult.getDownloaded().size());
//		assertEquals(0, downloadResult.getMissing().size());
//	}
//
//	@Test
//	public void testStatusFound() {
//		MovieRequest movieRequest = new MovieRequest("name", "hash");
//		Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);
//		Torrent torrent = new Torrent("title", "url", new Date(), 5);
//
////		when(torrentDao.find(any(Set.class))).thenReturn(Collections.<Torrent>emptyList());
//		SearchResult searchResult = new SearchResult("source");
//		searchResult.addDownloadable(torrent);
//		when(compositeMoviesSearcher.search(movieRequest)).thenReturn(searchResult);
//
//		DownloadConfig downloadConfig = new DownloadConfig();
//		downloadConfig.setForceDownload(false);
//		DownloadResult<Movie, MovieRequest> downloadResult = downloader.download(movieRequests, executor, downloadConfig);
//
//		assertEquals(1, downloadResult.getDownloaded().size());
//		assertEquals(0, downloadResult.getMissing().size());
//	}
//
//	@Test
//	public void testStatusFoundButOldYearInImdb() {
//		int curYear = Calendar.getInstance().get(Calendar.YEAR);
//		final MovieRequest movieRequest = new MovieRequest("Batman 1966 " + curYear, "hash"); // putting both current year and old year on same
//		Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);
//		Torrent torrent = new Torrent("title", "url", new Date(), 5);
//		SearchResult searchResult = new SearchResult("source");
//		searchResult.addDownloadable(torrent);
//		String imdbUrl = "some imdb url";
//		torrent.setImdbId(imdbUrl);
//
//		// using MatcherResult implementation cuz otherwise Mockito screws up the Matcher object
//		when(pageDownloader.downloadPageUntilFound(eq(imdbUrl), any(Pattern.class)))
//				.thenReturn("<meta name=\"title\" content=\"" + movieRequest.getTitle() + " (1966) - IMDb\" /><span itemprop=\"ratingCount\">1001</span>");
////		when(torrentDao.find(any(Set.class))).thenReturn(Collections.<Torrent>emptyList());
//		when(compositeMoviesSearcher.search(movieRequest)).thenReturn(searchResult);
//
//		DownloadConfig downloadConfig = new DownloadConfig();
//		downloadConfig.setForceDownload(false);
//		DownloadResult<Movie, MovieRequest> downloadResult = downloader.download(movieRequests, executor, downloadConfig);
//
//		assertEquals(0, downloadResult.getDownloaded().size());
//		assertEquals(0, downloadResult.getMissing().size());
//	}
//
//	@Test
//	public void testFindInCache() {
//		int curYear = Calendar.getInstance().get(Calendar.YEAR);
//		final MovieRequest movieRequest = new MovieRequest("Batman 1966 " + curYear, "hash"); // putting both current year and old year on same
//		Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);
//		Torrent torrent = new Torrent("title", "url", new Date(), 5);
//		SearchResult searchResult = new SearchResult("source");
//		searchResult.addDownloadable(torrent);
//		String imdbUrl = "some imdb url";
//		torrent.setImdbId(imdbUrl);
//
//		// using MatcherResult implementation cuz otherwise Mockito screws up the Matcher object
//		when(pageDownloader.downloadPageUntilFound(eq(imdbUrl), any(Pattern.class)))
//				.thenReturn("<meta name=\"title\" content=\"" + movieRequest.getTitle() + " (1966) - IMDb\" /><span itemprop=\"ratingCount\">1001</span>");
////		when(torrentDao.find(any(Set.class))).thenReturn(Collections.<Torrent>emptyList());
//		when(compositeMoviesSearcher.search(movieRequest)).thenReturn(searchResult);
//
//		DownloadConfig downloadConfig = new DownloadConfig();
//		downloadConfig.setForceDownload(false);
//		DownloadResult<Movie, MovieRequest> downloadResult = downloader.download(movieRequests, executor, downloadConfig);
//
//		assertEquals(0, downloadResult.getDownloaded().size());
//		assertEquals(0, downloadResult.getMissing().size());
//	}
//
//	@Test
//	public void testImdbTitleEscaping() {
//		final MovieRequest movieRequest = new MovieRequest("name", "hash");
//		Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);
//		Torrent torrent = new Torrent("title", "url", new Date(), 5);
//		SearchResult searchResult = new SearchResult("source");
//		searchResult.addDownloadable(torrent);
//		String imdbUrl = "http://www.imdb.com/title/tt2193021/";
//		torrent.setImdbId(imdbUrl);
//
//		// using MatcherResult implementation cuz otherwise Mockito screws up the Matcher object
//		when(pageDownloader.downloadPageUntilFound(eq(imdbUrl), any(Pattern.class))).then(new Answer<String>() {
//			@Override
//			public String answer(InvocationOnMock invocationOnMock) throws Throwable {
//				return loadPage("imdb-arrow");
//			}
//		});
////		when(torrentDao.find(any(Set.class))).thenReturn(Collections.<Torrent>emptyList());
//		when(compositeMoviesSearcher.search(movieRequest)).thenReturn(searchResult);
//
//		DownloadConfig downloadConfig = new DownloadConfig();
//		downloadConfig.setForceDownload(false);
//		DownloadResult<Movie, MovieRequest> downloadResult = downloader.download(movieRequests, executor, downloadConfig);
//
//		assertEquals(1, downloadResult.getDownloaded().size());
//		assertEquals(0, downloadResult.getMissing().size());
//		Movie movie = downloadResult.getDownloaded().iterator().next();
//		System.out.println(movie.getName());
//		assertEquals("Arrow (TV Series 2012– )", movie.getName());
//	}
//
//
//	@Test
//	public void testImdbHighViewersNumber_movieOk() {
//		final MovieRequest movieRequest = new MovieRequest("name", "hash");
//		Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);
//		Torrent torrent = new Torrent("title", "url", new Date(), 5);
//		SearchResult searchResult = new SearchResult("source");
//		searchResult.addDownloadable(torrent);
//		String imdbUrl = "http://www.imdb.com/title/tt2402582/";
//		torrent.setImdbId(imdbUrl);
//
//		// using MatcherResult implementation cuz otherwise Mockito screws up the Matcher object
//		when(pageDownloader.downloadPageUntilFound(eq(imdbUrl), any(Pattern.class))).then(new Answer<String>() {
//			@Override
//			public String answer(InvocationOnMock invocationOnMock) throws Throwable {
//				return loadPage("imdb-argo");
//			}
//		});
////		when(torrentDao.find(any(Set.class))).thenReturn(Collections.<Torrent>emptyList());
//		when(compositeMoviesSearcher.search(movieRequest)).thenReturn(searchResult);
//
//		DownloadConfig downloadConfig = new DownloadConfig();
//		downloadConfig.setForceDownload(false);
//		DownloadResult<Movie, MovieRequest> downloadResult = downloader.download(movieRequests, executor, downloadConfig);
//
//		assertEquals(1, downloadResult.getDownloaded().size());
//		assertEquals(0, downloadResult.getMissing().size());
//	}
//
//	@Test
//	public void testImdbLowViewersNumber_rejectMovie() {
//		final MovieRequest movieRequest = new MovieRequest("name", "hash");
//		Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);
//		Torrent torrent = new Torrent("title", "url", new Date(), 5);
//		SearchResult searchResult = new SearchResult("source");
//		searchResult.addDownloadable(torrent);
//		String imdbUrl = "http://www.imdb.com/title/tt2402582/";
//		torrent.setImdbId(imdbUrl);
//
//		// using MatcherResult implementation cuz otherwise Mockito screws up the Matcher object
//		when(pageDownloader.downloadPageUntilFound(eq(imdbUrl), any(Pattern.class))).then(new Answer<String>() {
//			@Override
//			public String answer(InvocationOnMock invocationOnMock) throws Throwable {
//				return loadPage("imdb-low-users");
//			}
//		});
////		when(torrentDao.find(any(Set.class))).thenReturn(Collections.<Torrent>emptyList());
//		when(compositeMoviesSearcher.search(movieRequest)).thenReturn(searchResult);
//
//		DownloadConfig downloadConfig = new DownloadConfig();
//		downloadConfig.setForceDownload(false);
//		DownloadResult<Movie, MovieRequest> downloadResult = downloader.download(movieRequests, executor, downloadConfig);
//
//		assertEquals(0, downloadResult.getDownloaded().size());
//		assertEquals(0, downloadResult.getMissing().size());
//	}
//
//	@Test
//	public void testComingSoonMovie_rejectMovie() {
//		final MovieRequest movieRequest = new MovieRequest("name", "hash");
//		Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);
//		Torrent torrent = new Torrent("title", "url", new Date(), 5);
//		SearchResult searchResult = new SearchResult("source");
//		searchResult.addDownloadable(torrent);
//		String imdbUrl = "url";
//		torrent.setImdbId(imdbUrl);
//
//		// using MatcherResult implementation cuz otherwise Mockito screws up the Matcher object
//		when(pageDownloader.downloadPageUntilFound(eq(imdbUrl), any(Pattern.class))).then(new Answer<String>() {
//			@Override
//			public String answer(InvocationOnMock invocationOnMock) throws Throwable {
//				return loadPage("future-movie-no-viewers");
//			}
//		});
////		when(torrentDao.find(any(Set.class))).thenReturn(Collections.<Torrent>emptyList());
//		when(compositeMoviesSearcher.search(movieRequest)).thenReturn(searchResult);
//
//		DownloadConfig downloadConfig = new DownloadConfig();
//		downloadConfig.setForceDownload(false);
//		DownloadResult<Movie, MovieRequest> downloadResult = downloader.download(movieRequests, executor, downloadConfig);
//
//		assertEquals(0, downloadResult.getDownloaded().size());
//		assertEquals(0, downloadResult.getMissing().size());
//	}
//}