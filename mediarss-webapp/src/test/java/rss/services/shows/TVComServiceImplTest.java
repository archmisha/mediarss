package rss.services.shows;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import rss.BaseTest;
import rss.dao.EpisodeDao;
import rss.entities.Episode;
import rss.entities.Show;
import rss.services.PageDownloader;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * User: dikmanm
 * Date: 30/12/12 22:42
 */
@RunWith(MockitoJUnitRunner.class)
public class TVComServiceImplTest extends BaseTest {

	@Mock
	private PageDownloader pageDownloader;
	@Mock
	private EpisodeDao episodeDao;

	@InjectMocks
	private ShowsProvider tvComService = new TVComServiceImpl();

	@Test
	public void testParseNormalTorrent() {
		String tvComUrl = "bla";
		Show show = new Show();
		show.setName("how i met your mother");
		show.setTvComUrl(tvComUrl);
		Episode ep = new Episode(8, 3);
		ep.setShow(show);
		show.getEpisodes().add(ep);
		String page = loadPage("how-i-met-your-mother");
		doReturn(page).when(pageDownloader).downloadPage(tvComUrl);

		Collection<Episode> episodes = tvComService.downloadSchedule(show);

		assertEquals(36, episodes.size());
		assertFalse(show.isEnded());
		for (Episode episode : episodes) {
			if (episode.getSeason() == 8 && episode.getEpisode() == 5) {
				Calendar c = Calendar.getInstance();
				c.set(Calendar.YEAR, 2012);
				c.set(Calendar.MONTH, 11 - 1);
				c.set(Calendar.DAY_OF_MONTH, 5);
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);
				Assert.assertEquals(c.getTime(), episode.getAirDate());
			}
		}
		verify(episodeDao, Mockito.times(0)).persist(any(Episode.class));
	}

	@Test
	public void testShowEnded() {
		String tvComUrl = "bla";
		Show show = new Show();
		show.setName("house");
		show.setTvComUrl(tvComUrl);
		String page = loadPage("house");
		doReturn(page).when(pageDownloader).downloadPage(tvComUrl);

		tvComService.downloadSchedule(show);

		assertTrue(show.isEnded());
	}

	@Test
	public void testParseNoAirDate() {
		String tvComUrl = "bla";
		Show show = new Show();
		show.setName("greys-anatomy");
		show.setTvComUrl(tvComUrl);
		String page = loadPage("greys-anatomy");
		doReturn(page).when(pageDownloader).downloadPage(tvComUrl);

		Collection<Episode> episodes = tvComService.downloadSchedule(show);

		assertFalse(show.isEnded());
		assertEquals(36, episodes.size());
	}

	// handles special episodes
	@Test
    @Ignore
	public void testAllEpisodesAreIn() {
		final Map<Integer, Set<Integer>> epMap = new HashMap<>();
		epMap.put(1, new HashSet<Integer>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)));
		epMap.put(2, new HashSet<Integer>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
		String tvComUrl = "bla";
		Show show = new Show();
		show.setName("revenge");
		show.setTvComUrl(tvComUrl);
		String page = loadPage("revenge");
		doReturn(page).when(pageDownloader).downloadPage(tvComUrl);

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				Episode episode = (Episode) invocationOnMock.getArguments()[0];
//				System.out.println(episode);
				epMap.get(episode.getSeason()).remove(episode.getEpisode());
				return null;
			}
		}).when(episodeDao).persist(any(Episode.class));

		tvComService.downloadSchedule(show);

		assertEquals(34, show.getEpisodes().size());
		assertFalse(show.isEnded());
		verify(episodeDao, Mockito.times(34)).persist(any(Episode.class));
		Assert.assertTrue(epMap.get(1).isEmpty());
		Assert.assertTrue(epMap.get(2).isEmpty());
	}

	@Test
	public void testSearchShowTheOC() {
		String tvComUrl = "http://www.tv.com/search?q=The+O.C.";
		String page = loadPage("the-o-c-search-result");
		doReturn(page).when(pageDownloader).downloadPage(tvComUrl);

		Show show = tvComService.search("The O.C.");

		assertNotNull(show);
		assertEquals("The O.C.", show.getName());
		assertTrue("is ended?", show.isEnded());
		assertEquals("http://www.tv.com/shows/the-oc/episodes/", show.getTvComUrl());
	}

	@Test
	public void testSearchShowNoResults() {
		String tvComUrl = "http://www.tv.com/search?q=dsdsd";
		String page = loadPage("no-search-results");
		doReturn(page).when(pageDownloader).downloadPage(tvComUrl);

		Show show = tvComService.search("dsdsd");

		assertNull(show);
	}

	@Test
	// test removing (US) at the end
	public void testSearchShowTheOffice() {
		final String page = loadPage("the-office-search-results");

		doAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocationOnMock) throws Throwable {
				String pageUrl = (String) invocationOnMock.getArguments()[0];
				if (pageUrl.equalsIgnoreCase("http://www.tv.com/search?q=The+Office")) {
					return page;
				}
				throw new RuntimeException("wrong url");
			}
		}).when(pageDownloader).downloadPage(anyString());

		Show show = tvComService.search("The Office (US)");

		assertNotNull(show);
		assertEquals("The Office", show.getName());
	}
}
