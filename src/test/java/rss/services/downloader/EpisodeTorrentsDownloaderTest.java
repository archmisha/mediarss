package rss.services.downloader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.transaction.support.TransactionTemplate;
import rss.BaseTest;
import rss.dao.EpisodeDao;
import rss.dao.TorrentDao;
import rss.entities.Episode;
import rss.entities.MediaQuality;
import rss.entities.Show;
import rss.services.EmailService;
import rss.services.PageDownloader;
import rss.services.requests.episodes.ShowRequest;
import rss.services.requests.episodes.SingleEpisodeRequest;
import rss.services.shows.ShowService;
import rss.services.shows.ShowsProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EpisodeTorrentsDownloaderTest extends BaseTest {

	@Mock
	private TorrentDao torrentDao;
	@Mock
	private EpisodeDao episodeDao;
	@Mock
	private EmailService emailService;
	@Mock
	private TransactionTemplate transactionTemplate;
	@Mock
	private ExecutorService executor;
	@Mock
	private PageDownloader pageDownloader;
	@Mock
	private ShowService showService;
	@Mock
	private ShowsProvider tvComService;

	@InjectMocks
	private EpisodeTorrentsDownloader downloader = new EpisodeTorrentsDownloader();

	@Before
	@Override
	public void setup() {
		super.setup();
		mockExecutorServiceAsDirectExecutor(executor);
		mockTransactionTemplate(transactionTemplate);
	}

	@Test
	public void testEpisodeFoundInCacheButNotTorrents() {
		Show show = new Show();
		ShowRequest episodeRequest = new SingleEpisodeRequest(null, "name", show, MediaQuality.HD720P, 2, 1);
		Set<ShowRequest> episodeRequests = Collections.singleton(episodeRequest);
		Episode episode = new Episode(2, 1);

		when(episodeDao.find(any(Collection.class))).thenReturn(Collections.singletonList(episode));
//		when(showService.findShow(any(String.class))).thenReturn(Collections.singletonList(show));
//		when(tvComService.getEpisodesCount(show, 2)).thenReturn(5);

		DownloadResult<Episode, ShowRequest> downloadResult = downloader.download(episodeRequests, executor, true);

		assertEquals(0, downloadResult.getDownloaded().size());
		assertEquals(1, downloadResult.getMissing().size());
	}
}