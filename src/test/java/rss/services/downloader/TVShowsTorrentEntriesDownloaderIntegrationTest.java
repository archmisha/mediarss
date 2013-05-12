package rss.services.downloader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.BaseTest;
import rss.dao.EpisodeDao;
import rss.dao.TorrentDao;
import rss.entities.Episode;
import rss.entities.MediaQuality;
import rss.entities.Show;
import rss.entities.Torrent;
import rss.services.requests.ShowRequest;
import rss.services.requests.SingleEpisodeRequest;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testContext.xml"})
@Transactional
public class TVShowsTorrentEntriesDownloaderIntegrationTest extends BaseTest {

	@Autowired
	private EpisodeTorrentsDownloader downloader = new EpisodeTorrentsDownloader();

	@Autowired
	private EpisodeDao episodeDao;

	@Autowired
	private TorrentDao torrentDao;

	@Test
	@Transactional(propagation = Propagation.REQUIRED)
	public void testFoundInCache() {
		Episode episode = new Episode(8, 1);
		Torrent torrent = new Torrent("How.I.Met.Your.Mother.S08E01.720p.HDTV.X264-DIMENSION [PublicHD]", "url", new Date(), 1);
		torrent.setQuality(MediaQuality.HD720P);
		episode.getTorrentIds().add(torrent.getId());
//		torrent.setMedia(episode);
		torrentDao.persist(torrent);
		episodeDao.persist(episode);

		ShowRequest episodeRequest = new SingleEpisodeRequest("how i met your mother", new Show(), MediaQuality.HD720P, 8, 1);
		Set<ShowRequest> episodeRequests = Collections.singleton(episodeRequest);
		ExecutorService executor = Mockito.mock(ExecutorService.class);

		Collection<Episode> download = downloader.download(episodeRequests, executor, true).getDownloaded();

		verify(executor, Mockito.times(0)).submit(any(Runnable.class));
		assertEquals(1, download.size());
	}
}