package rss.shows;

import org.apache.commons.io.IOUtils;
import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import rss.PageDownloader;
import rss.log.LogService;
import rss.rms.ResourceManagementService;
import rss.rms.RmsOperationsFactory;
import rss.rms.RmsResource;
import rss.rms.operation.get.GetResourcesRMSQuery;
import rss.rms.query.RmsQueryInformation;
import rss.rms.query.builder.RmsQueryBuilder;
import rss.shows.dao.ShowDao;
import rss.shows.dao.ShowImpl;
import rss.shows.providers.ShowData;
import rss.shows.providers.SyncData;
import rss.shows.providers.TheTVDbServiceImpl;
import rss.shows.thetvdb.TheTvDbSyncTime;
import rss.torrents.Episode;
import rss.torrents.Show;

import java.io.IOException;
import java.util.*;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Created by dikmanm on 29/10/2015.
 */
public class TheTVDbServiceImplTest {

    public static final long MOCK_THETVDB_SHOW_ID = 73255;
    public static final long MOCK_THETVDB_EPISODE_ID = 110996;
    public static final int MOCK_EPISODE_SEASON_NUMBER = 1;
    public static final int MOCK_EPISODE_EPISODE_NUMBER = 3;

    private IMocksControl mocksControl;
    private PageDownloader pageDownloaderMock;
    private ResourceManagementService rmsServiceMock;
    private LogService logServiceMock;
    private ShowDao showDaoMock;
    private TheTVDbServiceImpl service;

    @Before
    public void before() {
        mocksControl = createControl();
        pageDownloaderMock = mocksControl.createMock(PageDownloader.class);
        rmsServiceMock = mocksControl.createMock(ResourceManagementService.class);
        logServiceMock = mocksControl.createMock(LogService.class);
        showDaoMock = mocksControl.createMock(ShowDao.class);
        service = new TheTVDbServiceImpl();
        ReflectionTestUtils.setField(service, "pageDownloader", pageDownloaderMock);
        ReflectionTestUtils.setField(service, "rmsService", rmsServiceMock);
        ReflectionTestUtils.setField(service, "showDao", showDaoMock);
        ReflectionTestUtils.setField(service, "logService", logServiceMock);
    }

    @Test
    public void testSearchNoResult() {
        String name = "House";
        String page = "<Data></Data>";
        expect(pageDownloaderMock.downloadPage(TheTVDbServiceImpl.SEARCH_URL + name)).andReturn(page);

        mocksControl.replay();
        Show show = service.search(name);
        mocksControl.verify();

        assertNull(show);
    }

    @Test
    public void testSearch() {
        Show show = new ShowImpl("House");
        show.setTheTvDbId(MOCK_THETVDB_SHOW_ID);
        String page = getShowDataString(show);
        expect(pageDownloaderMock.downloadPage(TheTVDbServiceImpl.SEARCH_URL + show.getName())).andReturn(page);

        mocksControl.replay();
        Show showResult = service.search(show.getName());
        mocksControl.verify();

        assertNotNull(show);
        assertEquals(show.getTheTvDbId(), showResult.getTheTvDbId());
        assertEquals(show.getName(), showResult.getName());
    }

    @Test
    public void testDownloadShowList_syncTimeExists() {
        TheTvDbSyncTime syncTime = new TheTvDbSyncTime();
        mockRmsGetOperation(TheTvDbSyncTime.class, syncTime);
        expect(showDaoMock.getShowsWithoutTheTvDbId()).andReturn(Collections.<Show>emptyList());

        mocksControl.replay();
        service.downloadShowList();
        mocksControl.verify();
    }

    @Test
    public void testDownloadShowList_syncTimeNotExists() {
        final long time = 123;
        String serverSyncTimeResult = "<Update><Time>" + time + "</Time></Update>";
        expect(pageDownloaderMock.downloadPage(TheTVDbServiceImpl.SERVER_TIME_URL)).andReturn(serverSyncTimeResult);
        mockRmsGetOperation(TheTvDbSyncTime.class, null);
        mockRmsSaveSyncTime(time);
        expect(showDaoMock.getShowsWithoutTheTvDbId()).andReturn(Collections.<Show>emptyList());

        mocksControl.replay();
        service.downloadShowList();
        mocksControl.verify();
    }

    @Test
    public void testDownloadShowList() {
        TheTvDbSyncTime syncTime = new TheTvDbSyncTime();
        mockRmsGetOperation(TheTvDbSyncTime.class, syncTime);

        Show show = new ShowImpl("House");
        show.setTheTvDbId(MOCK_THETVDB_SHOW_ID);
        expect(showDaoMock.getShowsWithoutTheTvDbId()).andReturn(Arrays.asList(show));
        String page = getShowDataString(show);
        expect(pageDownloaderMock.downloadPage(TheTVDbServiceImpl.SEARCH_URL + show.getName())).andReturn(page);

        mocksControl.replay();
        Collection<Show> shows = service.downloadShowList();
        mocksControl.verify();

        assertEquals(1, shows.size());
        Show showResult = shows.iterator().next();
        assertEquals(show, showResult);

    }

    @Test
    public void testGetShowData() throws IOException {
        Show show = new ShowImpl("House");
        show.setTheTvDbId(MOCK_THETVDB_SHOW_ID);

        byte[] zipData = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("thetvdb/en.zip"));
        expect(pageDownloaderMock.downloadData(String.format(TheTVDbServiceImpl.SHOW_URL, show.getTheTvDbId()))).andReturn(zipData);

        mocksControl.replay();
        ShowData showData = service.getShowData(show);
        mocksControl.verify();

        assertEquals(true, showData.getShow().isEnded());
        assertTrue(showData.getEpisodes().size() > 0);
        Episode episode = null;
        for (Episode curEpisode : showData.getEpisodes()) {
            if (curEpisode.getSeason() == MOCK_EPISODE_SEASON_NUMBER && curEpisode.getEpisode() == MOCK_EPISODE_EPISODE_NUMBER) {
                episode = curEpisode;
                break;
            }
        }
        assertNotNull(episode);
        assertEquals(MOCK_THETVDB_EPISODE_ID, episode.getTheTvDbId());
        assertEquals(getDate(30, 11, 2004), episode.getAirDate());
    }

    @Test
    public void testGetSyncData_noUpdates() {
        long time = 123;
        long newTime = 456;
        TheTvDbSyncTime syncTime = new TheTvDbSyncTime();
        syncTime.setTime(time);
        mockRmsGetOperation(TheTvDbSyncTime.class, syncTime);
        mockRmsSaveSyncTime(newTime);
        expect(pageDownloaderMock.downloadPage(TheTVDbServiceImpl.UPDATE_URL + time)).andReturn(getSyncDataString(newTime, null, null));

        mocksControl.replay();
        SyncData syncData = service.getSyncData();
        mocksControl.verify();

        assertTrue(syncData.getEpisodes().isEmpty());
        assertTrue(syncData.getShows().isEmpty());
    }

    @Test
    public void testGetSyncData_episodeAndShowWithSameEpisode() throws IOException {
        long time = 123;
        long newTime = 456;
        TheTvDbSyncTime syncTime = new TheTvDbSyncTime();
        syncTime.setTime(time);
        mockRmsGetOperation(TheTvDbSyncTime.class, syncTime);
        mockRmsSaveSyncTime(newTime);
        expect(pageDownloaderMock.downloadPage(TheTVDbServiceImpl.UPDATE_URL + time)).andReturn(getSyncDataString(newTime, MOCK_THETVDB_SHOW_ID, MOCK_THETVDB_EPISODE_ID));
        byte[] zipData = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("thetvdb/en.zip"));
        expect(pageDownloaderMock.downloadData(String.format(TheTVDbServiceImpl.SHOW_URL, MOCK_THETVDB_SHOW_ID))).andReturn(zipData);

        mocksControl.replay();
        SyncData syncData = service.getSyncData();
        mocksControl.verify();

        assertEquals(1, syncData.getShows().size());
        Show resultShow = syncData.getShows().get(0);
        assertEquals(MOCK_THETVDB_SHOW_ID, resultShow.getTheTvDbId());
        assertTrue(resultShow.isEnded());

        assertEquals(1, syncData.getEpisodes().size());
        assertEquals(MOCK_THETVDB_EPISODE_ID, syncData.getEpisodes().get(0).getTheTvDbId());
        assertEquals(getDate(30, 11, 2004), syncData.getEpisodes().get(0).getAirDate());
    }

    private <T extends RmsResource> void mockRmsGetOperation(Class clazz, T result) {
        RmsOperationsFactory factoryMock = mocksControl.createMock(RmsOperationsFactory.class);
        GetResourcesRMSQuery getResourcesRmsQueryMock = mocksControl.createMock(GetResourcesRMSQuery.class);
        expect(factoryMock.createGetResourceOperation(eq(clazz), anyObject(RmsQueryInformation.class))).andReturn(getResourcesRmsQueryMock);
        RmsQueryBuilder rmsQueryBuilder = mocksControl.createMock(RmsQueryBuilder.class);
        expect(rmsQueryBuilder.getRmsQueryInformation()).andReturn(mocksControl.createMock(RmsQueryInformation.class));
        expect(factoryMock.createRmsQueryBuilder()).andReturn(rmsQueryBuilder);
        expect(rmsServiceMock.apiFactory()).andReturn(factoryMock).anyTimes();
        expect(rmsServiceMock.get(getResourcesRmsQueryMock)).andReturn(result);
    }

    private void mockRmsSaveSyncTime(final long time) {
        rmsServiceMock.saveOrUpdate(anyObject(TheTvDbSyncTime.class), eq(TheTvDbSyncTime.class));
        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                TheTvDbSyncTime theTvDbSyncTime = (TheTvDbSyncTime) getCurrentArguments()[0];
                assertEquals(time, theTvDbSyncTime.getTime());
                return null;
            }
        });
    }

    private String getSyncDataString(long time, Long showId, Long episodeId) {
        String result = "<Items><Time>" + time + "</Time>";
        if (showId != null) {
            result += "<Series>" + showId + "</Series>";
        }
        if (episodeId != null) {
            result += "<Episode>" + episodeId + "</Episode>";
        }
        result += "</Items>";
        return result;
    }

    private String getEpisodeDataString(Episode episode) {
        return "<Data>\n" +
                "<Episode>\n" +
                "<id>" + episode.getTheTvDbId() + "</id>\n" +
                "<seasonid>23</seasonid>\n" +
                "<EpisodeNumber>" + episode.getEpisode() + "</EpisodeNumber>\n" +
                "<EpisodeName>Ep. #7573</EpisodeName>\n" +
                "<FirstAired>2003-02-20</FirstAired>\n" +
                "<GuestStars/>\n" +
                "<Director/>\n" +
                "<Writer/>\n" +
                "<Overview/>\n" +
                "<ProductionCode/>\n" +
                "<lastupdated>1163101692</lastupdated>\n" +
                "<flagged>0</flagged>\n" +
                "<DVD_discid/>\n" +
                "<DVD_season/>\n" +
                "<DVD_episodenumber/>\n" +
                "<DVD_chapter/>\n" +
                "<absolute_number/>\n" +
                "<filename/>\n" +
                "<seriesid>70328</seriesid>\n" +
                "<thumb_added/>\n" +
                "<thumb_width/>\n" +
                "<thumb_height/>\n" +
                "<tms_export/>\n" +
                "<mirrorupdate>2013-11-08 07:33:29</mirrorupdate>\n" +
                "<IMDB_ID/>\n" +
                "<EpImgFlag/>\n" +
                "<Rating>0</Rating>\n" +
                "<SeasonNumber>" + episode.getSeason() + "</SeasonNumber>\n" +
                "<Language>en</Language>\n" +
                "</Episode>\n" +
                "</Data>";
    }

    private String getShowDataString(Show show) {
        return "<Data>" +
                "<Series>\n" +
                "<seriesid>" + show.getTheTvDbId() + "</seriesid>\n" +
                "<language>en</language>\n" +
                "<SeriesName>" + show.getName() + "</SeriesName>\n" +
                "<AliasNames>House M.D.</AliasNames>\n" +
                "<banner>graphical/73255-g28.jpg</banner>\n" +
                "<Overview>\n" +
                "Go deeper into the medical mysteries of House, TV's most compelling drama. Hugh Laurie stars as the brilliant but sarcastic Dr. Gregory House, a maverick physician who is devoid of bedside manner. While his behavior can border on antisocial, Dr. House thrives on the challenge of solving the medical puzzles that other doctors give up on. Together with his hand-picked team of young medical experts, he'll do whatever it takes in the race against the clock to solve the case.\n" +
                "</Overview>\n" +
                "<FirstAired>2004-11-16</FirstAired>\n" +
                "<Network>FOX (US)</Network>\n" +
                "<IMDB_ID>tt0412142</IMDB_ID>\n" +
                "<zap2it_id>EP00688359</zap2it_id>\n" +
                "<id>" + show.getTheTvDbId() + "</id>\n" +
                "</Series>" +
                "</Data>";
    }

    private Date getDate(int day, int month, int year) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
}
