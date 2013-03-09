package rss.services.shows;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import rss.BaseTest;
import rss.dao.EpisodeDao;
import rss.entities.Episode;
import rss.entities.Show;
import rss.services.PageDownloader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

/**
 * User: dikmanm
 * Date: 30/12/12 22:42
 */
@RunWith(MockitoJUnitRunner.class)
public class TVRageServiceImplTest extends BaseTest {

	@Mock
	private PageDownloader pageDownloader;

	@Mock
	private EpisodeDao episodeDao;

	@InjectMocks
	private ShowsProvider tvRageService = new TVRageServiceImpl();

	@Test
	public void testDownloadShowList() {
		String page = loadPage("tvrage-showlist-2013-02-25.xml");
		doReturn(page).when(pageDownloader).downloadPage(anyString());

		Collection<Show> shows = tvRageService.downloadShowList();

		int counter = 0;
		for (Show show : shows) {
			if (show.getName().equals("House")) {
				Assert.assertTrue(show.isEnded());
				counter++;
			} else if (show.getName().equals("Futurama")) {
				Assert.assertFalse(show.isEnded());
				counter++;
			}
		}
		Assert.assertEquals(2, counter);
	}

	@Test
	public void testDownloadInfo() {
		try {
			String page = loadPage("tvrage-futurama-info.xml");
			doReturn(page).when(pageDownloader).downloadPage(anyString());
			Show show = new Show();
			show.setTvRageId(3628);
			Collection<Episode> episodes = tvRageService.downloadInfo(show);

			Assert.assertEquals(111, episodes.size());

			Episode episode = episodes.iterator().next();
			Assert.assertEquals(1, episode.getEpisode());

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Assert.assertEquals(sdf.parse("1999-03-28"), episode.getAirDate());
			Assert.assertTrue(show.isEnded());
		} catch (ParseException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testDownloadInfo2() {
		String page = loadPage("tvrage-fernwood-2night-info.xml");
		doReturn(page).when(pageDownloader).downloadPage(anyString());
		Show show = new Show();
		show.setTvRageId(1);
		Collection<Episode> episodes = tvRageService.downloadInfo(show);

		Assert.assertEquals(65 - 4, episodes.size()); // minus 4 for special
	}

	@Test
	public void testDownloadInfo3() {
		String page = loadPage("tvrage-keep-up-appearances-info.xml");
		doReturn(page).when(pageDownloader).downloadPage(anyString());
		Show show = new Show();
		show.setTvRageId(1);
		Collection<Episode> episodes = tvRageService.downloadInfo(show);

		Assert.assertEquals(44, episodes.size());
	}

	@Test
	public void testDownloadSchedule() {
		String page = loadPage("tvrage-schedule-2013-02-25.xml");
		doReturn(page).when(pageDownloader).downloadPage(anyString());
		Collection<Episode> episodes = tvRageService.downloadSchedule();
		Assert.assertEquals(1632 - 8, episodes.size()); // minus special
	}
}
