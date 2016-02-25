//package rss.torrents.downloader;
//
//import org.junit.Ignore;
//import org.junit.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//import rss.BaseTest;
//import rss.movies.dao.MovieDao;
//import rss.torrents.TorrentDao;
//import rss.torrents.Movie;
//import rss.torrents.requests.movies.MovieRequest;
//import rss.torrents.Torrent;
//
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Date;
//import java.util.Set;
//import java.util.concurrent.ExecutorService;
//
//import static org.junit.Assert.assertEquals;
//import static org.mockito.Mockito.any;
//import static org.mockito.Mockito.verify;
//
////@RunWith(SpringJUnit4ClassRunner.class)
////@ContextConfiguration(locations = {"classpath:testContext.xml"})
//@Transactional
//@Ignore
//public class MoviesTorrentEntriesDownloaderIntegrationTest extends BaseTest {
//
//    @Autowired
//    private MovieTorrentsDownloaderImpl downloader = new MovieTorrentsDownloaderImpl();
//
//    @Autowired
//    private MovieDao movieDao;
//
//    @Autowired
//    private TorrentDao torrentDao;
//
//    @Test
//    @Transactional(propagation = Propagation.REQUIRED)
//    public void testFoundInCache() {
//        // name is not always the same but hash is
//        Movie movie = new Movie("Fire with Fire (Video 2012)", "imdb url", 2012, new Date());
//        Torrent torrent = new Torrent("Fire.With.Fire.2012.BluRay.720p.DTS.x264-CHD", "url", new Date(), 1);
//        String hash = "my hash";
//        torrent.setHash(hash);
//        movie.getTorrentIds().add(torrent.getId());
////		torrent.setMedia(movie);
//        torrentDao.persist(torrent);
//        movieDao.persist(movie);
//
//        MovieRequest movieRequest = new MovieRequest("bla", hash);
//        Set<MovieRequest> movieRequests = Collections.singleton(movieRequest);
//        ExecutorService executor = Mockito.mock(ExecutorService.class);
//
//        DownloadConfig downloadConfig = new DownloadConfig();
//        downloadConfig.setForceDownload(true);
//        Collection<Movie> download = downloader.download(movieRequests, executor, downloadConfig).getDownloaded();
//
//        verify(executor, Mockito.times(0)).submit(any(Runnable.class));
//        assertEquals(1, download.size());
//    }
//}