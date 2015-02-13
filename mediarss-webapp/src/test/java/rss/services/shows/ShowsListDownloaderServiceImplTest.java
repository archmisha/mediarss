package rss.services.shows;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import rss.BaseTest;
import rss.dao.ShowDao;
import rss.entities.Show;
import rss.services.PageDownloader;
import rss.services.SettingsService;

import java.util.Collection;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;

/**
 * User: dikmanm
 * Date: 30/12/12 22:42
 */
@RunWith(MockitoJUnitRunner.class)
public class ShowsListDownloaderServiceImplTest extends BaseTest {

	private static final int PAGES_TO_DOWNLOAD = 1;

	@Mock
	private PageDownloader pageDownloader;

	@Mock
	private ShowDao showDao;

	@Mock
	private SettingsService settingsService;

	@InjectMocks
	private TVComServiceImpl tvComService = new TVComServiceImpl();

	@Test
	public void testFirstPageParsing() {
		String page = loadPage("tvcom-page1");
		doReturn(page).when(pageDownloader).downloadPage(anyString());
		doReturn(PAGES_TO_DOWNLOAD).when(settingsService).getTVComPagesToDownload();

		Collection<Show> shows = tvComService.downloadShowList();

		Assert.assertEquals(20, shows.size());

		boolean isFound = false;
		for (Show show : shows) {
			if (show.getName().equals("Gossip Girl")) {
				isFound = true;
				Assert.assertTrue(show.isEnded());
			}
		}
		Assert.assertTrue(isFound);
	}

	@Test
	public void testSecondPageParsing() {
		String page2 = loadPage("tvcom-page2");
		doReturn(page2).when(pageDownloader).downloadPage(anyString());
		doReturn(1).when(settingsService).getTVComPagesToDownload();

		Collection<Show> shows = tvComService.downloadShowList();

		Assert.assertEquals(20, shows.size());
	}
}
