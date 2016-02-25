package rss.shows;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import rss.cache.CachedShowSubsetSet;
import rss.cache.ShowsCacheService;
import rss.log.LogService;
import rss.shows.ShowSearchServiceImpl;
import rss.shows.providers.ShowsProvider;
import rss.torrents.MediaQuality;
import rss.torrents.requests.shows.FullShowRequest;

import java.util.ArrayList;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by dikmanm on 31/01/2016.
 */
public class ShowSearchServiceImplTest {

    private ShowSearchServiceImpl tested;
    private IMocksControl mocksControl;
    private ShowsProvider showsProviderMock;
    private LogService logServiceMock;
    private ShowsCacheService showsCacheServiceMock;

    @Before
    public void before() {
        mocksControl = createControl();
        showsProviderMock = mocksControl.createMock(ShowsProvider.class);
        logServiceMock = mocksControl.createMock(LogService.class);
        showsCacheServiceMock = mocksControl.createMock(ShowsCacheService.class);
        tested = new ShowSearchServiceImpl();
        ReflectionTestUtils.setField(tested, "showsProvider", showsProviderMock);
        ReflectionTestUtils.setField(tested, "logService", logServiceMock);
        ReflectionTestUtils.setField(tested, "showsCacheService", showsCacheServiceMock);

        logServiceMock.debug(anyObject(Class.class), anyObject(String.class));
        expectLastCall().anyTimes();
        logServiceMock.info(anyObject(Class.class), anyObject(String.class));
        expectLastCall().anyTimes();
    }

    @Test
    public void testSearchNotFoundShow() {
        FullShowRequest showRequest = new FullShowRequest(1L, "a", null, MediaQuality.HD720P);
        expect(showsProviderMock.search(showRequest.getTitle())).andReturn(null);
        expect(showsCacheServiceMock.getShowsSubsets()).andReturn(new ArrayList<CachedShowSubsetSet>());
        expect(showsCacheServiceMock.getAll()).andReturn(new ArrayList<CachedShow>());

        mocksControl.replay();
        SearchResultJSON searchResultJSON = tested.search(showRequest, 1L, false);
        mocksControl.verify();

        assertEquals(showRequest.getTitle(), searchResultJSON.getActualSearchTerm());
        assertEquals(showRequest.getTitle(), searchResultJSON.getOriginalSearchTerm());
        assertTrue(searchResultJSON.getEpisodes().isEmpty());
    }
}
