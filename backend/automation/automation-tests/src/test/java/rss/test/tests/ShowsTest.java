package rss.test.tests;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import rss.shows.SearchResultJSON;
import rss.shows.ShowAutoCompleteJSON;
import rss.shows.ShowJSON;
import rss.shows.schedule.ShowsDaySchedule;
import rss.shows.schedule.ShowsScheduleJSON;
import rss.shows.thetvdb.TheTvDbShow;
import rss.test.entities.UserData;
import rss.test.services.AdminService;
import rss.test.services.TestPagesService;
import rss.test.shows.ShowsService;
import rss.test.shows.TheTvDbEpisodeBuilder;
import rss.test.shows.TheTvDbShowBuilder;
import rss.test.util.WaitUtil;
import rss.torrents.DownloadStatus;
import rss.torrents.UserTorrentJSON;
import rss.util.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

/**
 * User: dikmanm
 * Date: 16/08/2015 14:37
 */
public class ShowsTest extends BaseTest {

    @Autowired
    private ShowsService showsService;

    @Autowired
    private TestPagesService testPagesService;

    @Autowired
    private TheTvDbShowBuilder theTvDbShowBuilder;

    @Autowired
    private TheTvDbEpisodeBuilder theTvDbEpisodeBuilder;

    @Autowired
    private AdminService adminService;

    @Test
    public void testTrackedShows() {
        reporter.info("Prepare data");
        UserData adminUser = userService.createAdminUser();
        userService.login(adminUser);
        reporter.info("Create 2 shows, 1 ended and 1 not");
        TheTvDbShow notEndedTheTvDbShow = theTvDbShowBuilder.aRunningShow().build();
        TheTvDbShow endedTheTvDbShow = theTvDbShowBuilder.anEndedShow().build();
        adminService.runDownloadShowsScheduleJob();

        ShowJSON nonEndedShow = showsService.search(notEndedTheTvDbShow.getName()).getShow();
        ShowJSON endedShow = showsService.search(endedTheTvDbShow.getName()).getShow();
        assertNotNull(nonEndedShow);
        assertNotNull(endedShow);

        reporter.info("Get tracked shows and verify empty list");
        List<ShowJSON> shows = showsService.getTrackedShows();
        assertEquals(0, shows.size());

        reporter.info("Add one ended and one not ended tracked shows");
        showsService.addTrackedShow(endedShow);
        showsService.addTrackedShow(nonEndedShow);

        reporter.info("Get tracked shows and verify 2 shows are returned");
        shows = showsService.getTrackedShows();
        assertEquals(2, shows.size());
        for (ShowJSON show : shows) {
            if (show.getName().equals(nonEndedShow.getName())) {
                assertEquals(false, show.isEnded());
            } else if (show.getName().equals(endedShow.getName())) {
                assertEquals(true, show.isEnded());
            }
        }

        reporter.info("Remove tracked show");
        showsService.removeTrackedShow(endedShow);

        reporter.info("Get tracked shows and verify removed show does not return");
        shows = showsService.getTrackedShows();
        assertEquals(1, shows.size());

        reporter.info("Simulate show has ended");
        testPagesService.setShowEnded(notEndedTheTvDbShow);
        adminService.runDownloadShowsScheduleJob();

        reporter.info("Get tracked shows and verify not ended show is now ended");
        shows = showsService.getTrackedShows();
        assertEquals(1, shows.size());
        assertEquals("Show '" + shows.get(0).getName() + "' is not marked ended", true, shows.get(0).isEnded());
    }

    @Test
    public void testSchedule() {
        // episodes schedule is downloaded either with the schedule job or when a tracked show is added
        // so add one show with episodes and one show without episodes and call schedule job
        // then add episodes to the second show and add it as tracked show, this will trigger the download of its episodes as well

        reporter.info("Prepare data");
        UserData adminUser = userService.createAdminUser();
        userService.login(adminUser);
        TheTvDbShow theTvDbShow1 = theTvDbShowBuilder.aRunningShow().withName("show1").build();
        TheTvDbShow theTvDbShow2 = theTvDbShowBuilder.aRunningShow().withName("show2").build();

        reporter.info("Add episodes to first show");
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow1, 1, 1).withAirDate(getDate(-20)).build(); // should not return
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow1, 1, 2).withAirDate(getDate(-12)).build();
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow1, 1, 3).withAirDate(getDate(-11)).build();
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow1, 1, 4).withAirDate(getDate(-10)).build();
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow1, 1, 5).withAirDate(getDate(-10)).build();
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow1, 1, 6).withAirDate(getDate(-9)).build();
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow1, 1, 7).withAirDate(getDate(-8)).build();
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow1, 1, 8).withAirDate(getDate(-1)).build();
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow1, 1, 9).withAirDate(getDate(0)).build();

        adminService.runDownloadShowsScheduleJob();

        ShowJSON show1 = showsService.getShow(theTvDbShow1.getName());
        ShowJSON show2 = showsService.getShow(theTvDbShow2.getName());
        assertNotNull(show1);
        assertNotNull(show2);

        reporter.info("Get schedule for user and verify its empty");
        ShowsScheduleJSON schedule = showsService.getSchedule();
        assertEquals(1, schedule.getSchedules().size());
        assertTrue(DateUtils.isSameDay(System.currentTimeMillis(), schedule.getSchedules().get(0).getDate()));
        assertEquals(0, schedule.getSchedules().get(0).getShows().size());

        reporter.info("Add episodes of second show");
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow2, 2, 1).withAirDate(getDate(-1)).build();
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow2, 2, 2).withAirDate(getDate(0)).build();
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow2, 2, 3).withAirDate(getDate(+1)).build();
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow2, 2, 4).withAirDate(getDate(+1)).build();
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow2, 2, 5).withAirDate(getDate(+1)).build();
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow2, 2, 6).withAirDate(getDate(+8)).build();
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow2, 2, 7).withAirDate(getDate(+9)).build();
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow2, 2, 8).withAirDate(getDate(+10)).build();
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow2, 2, 9).withAirDate(getDate(+11)).build();
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow2, 2, 10).withAirDate(getDate(+12)).build(); // should not return

        reporter.info("Add tracked shows to user");
        showsService.addTrackedShow(show1);
        showsService.addTrackedShow(show2);

        reporter.info("Get schedule for user and verify it contains added shows schedule");
        // single episode for day before yesterday
        schedule = showsService.getSchedule();
        assertEquals(13, schedule.getSchedules().size());
        ShowsDaySchedule prevPrevDaySchedule = schedule.getSchedules().get(4);
        assertTrue(DateUtils.isSameDay(getDate(-8).getTime(), prevPrevDaySchedule.getDate()));
        assertEquals(1, prevPrevDaySchedule.getShows().size());

        // combined 2 shows on today - should be sorted by show name
        ShowsDaySchedule curDaySchedule = schedule.getSchedules().get(6);
        assertTrue(DateUtils.isSameDay(System.currentTimeMillis(), curDaySchedule.getDate()));
        assertEquals(2, curDaySchedule.getShows().size());
        assertEquals(theTvDbShow1.getName(), curDaySchedule.getShows().get(0).getShowName());
        assertEquals("S01E09", curDaySchedule.getShows().get(0).getSequence());
        assertEquals(theTvDbShow2.getName(), curDaySchedule.getShows().get(1).getShowName());
        assertEquals("S02E02", curDaySchedule.getShows().get(1).getSequence());

        // multiple episodes of the same show on the same day, sorted by episode
        ShowsDaySchedule nextDaySchedule = schedule.getSchedules().get(7);
        assertTrue(DateUtils.isSameDay(getDate(+1).getTime(), nextDaySchedule.getDate()));
        assertEquals(3, nextDaySchedule.getShows().size());
        assertEquals(theTvDbShow2.getName(), nextDaySchedule.getShows().get(0).getShowName());
        assertEquals("S02E03", nextDaySchedule.getShows().get(0).getSequence());
        assertEquals(theTvDbShow2.getName(), nextDaySchedule.getShows().get(1).getShowName());
        assertEquals("S02E04", nextDaySchedule.getShows().get(1).getSequence());
        assertEquals(theTvDbShow2.getName(), nextDaySchedule.getShows().get(2).getShowName());
        assertEquals("S02E05", nextDaySchedule.getShows().get(2).getSequence());

        reporter.info("Remove tracked show from user");
        showsService.removeTrackedShow(show1);
        showsService.removeTrackedShow(show2);

        reporter.info("Get schedule for user and verify its empty again");
        schedule = showsService.getSchedule();
        assertEquals(1, schedule.getSchedules().size());
        assertTrue(DateUtils.isSameDay(System.currentTimeMillis(), schedule.getSchedules().get(0).getDate()));
        assertEquals(0, schedule.getSchedules().get(0).getShows().size());
    }

    @Test
    public void testAutoCompleteTrackedShows() {
        reporter.info("Prepare data");
        UserData adminUser = userService.createAdminUser();
        userService.login(adminUser);
        String unique = this.unique.unique();
        TheTvDbShow trackedByUserTheTvDbShow = theTvDbShowBuilder.aRunningShow().withName(unique + "show1").build();
        TheTvDbShow endedTheTvDbShow = theTvDbShowBuilder.anEndedShow().withName(unique + "show3").build();
        TheTvDbShow notTracked1TheTvDbShow = theTvDbShowBuilder.aRunningShow().withName(unique + "show2").build();
        TheTvDbShow notTracked2TheTvDbShow = theTvDbShowBuilder.aRunningShow().withName(unique + "sho").build();
        TheTvDbShow notTracked3TheTvDbShow = theTvDbShowBuilder.aRunningShow().withName(unique + "shot").build();
        adminService.runDownloadShowsScheduleJob();
        ShowJSON trackedByUser = showsService.getShow(trackedByUserTheTvDbShow.getName());
        ShowJSON ended = showsService.getShow(endedTheTvDbShow.getName());
        ShowJSON notTracked1 = showsService.getShow(notTracked1TheTvDbShow.getName());
        ShowJSON notTracked2 = showsService.getShow(notTracked2TheTvDbShow.getName());
        ShowJSON notTracked3 = showsService.getShow(notTracked3TheTvDbShow.getName());
        assertNotNull(trackedByUser);
        assertNotNull(ended);
        assertNotNull(notTracked1);
        assertNotNull(notTracked2);
        assertNotNull(notTracked3);
        showsService.addTrackedShow(trackedByUser);

        reporter.info("Test auto-complete not returns shows already tracked by user and ended shows");
        ShowAutoCompleteJSON showAutoCompleteJSON = showsService.autoCompleteTracked(unique + "show");
        assertEquals(1, showAutoCompleteJSON.getTotal());
        assertEquals(notTracked1TheTvDbShow.getName(), showAutoCompleteJSON.getShows().get(0).getText());

        reporter.info("Test auto-complete returns multiple items sorted by name");
        showAutoCompleteJSON = showsService.autoCompleteTracked(unique + "sho");
        assertEquals(3, showAutoCompleteJSON.getTotal());
        assertEquals(notTracked2TheTvDbShow.getName(), showAutoCompleteJSON.getShows().get(0).getText());
        assertEquals(notTracked3TheTvDbShow.getName(), showAutoCompleteJSON.getShows().get(1).getText());
        assertEquals(notTracked1TheTvDbShow.getName(), showAutoCompleteJSON.getShows().get(2).getText());

        reporter.info("Test auto-complete returns no results");
        showAutoCompleteJSON = showsService.autoCompleteTracked(unique + "shoz");
        assertEquals(0, showAutoCompleteJSON.getTotal());
        assertEquals(0, showAutoCompleteJSON.getShows().size());
    }

    @Test
    public void testDownloadEpisode_search_getAndRemoveStatus() {
        reporter.info("Prepare data");
        UserData adminUser = userService.createAdminUser();
        userService.login(adminUser);
        TheTvDbShow theTvDbShow = theTvDbShowBuilder.aRunningShow().build();
        adminService.runDownloadShowsScheduleJob();
        ShowJSON show = showsService.getShow(theTvDbShow.getName());
        assertNotNull(show);
        UserData user = userService.createUser();
        userService.login(user);

        reporter.info("Search for episode not in schedule and get immediate result");
        SearchResultJSON searchResult = showsService.search(show, 1, 2);
        assertEquals(0, searchResult.getEpisodesCount());
        assertNotNull(searchResult.getStart());
        assertNotNull(searchResult.getEnd());

        reporter.info("Add an episode and a torrent for the episode to be searched next and appear in progress");
        userService.login(adminUser);
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow, 1, 1).build();
        testPagesService.addTorrent(show, 1, 1);
        userService.login(user);

        reporter.info("Search and get in progress status");
        searchResult = showsService.search(show, 1, 1);
        assertEquals(0, searchResult.getEpisodesCount());
        assertNotNull(searchResult.getStart());
        assertNull(searchResult.getEnd());

        reporter.info("Get status of the download");
        searchResult = showsService.getSearchStatusSingleWithPolling();
        String heavySearchId = searchResult.getId();
        assertEquals(1, searchResult.getEpisodesCount());
        UserTorrentJSON userTorrent = searchResult.getEpisodes().iterator().next();

        reporter.info("Check torrent was never downloaded");
        assertEquals(DownloadStatus.NONE, userTorrent.getDownloadStatus());
        assertNull(userTorrent.getDownloadDate());

        reporter.info("Download episode for the first time");
        showsService.downloadEpisode(userTorrent.getTorrentId());

        reporter.info("Check torrent marked as scheduled");
        searchResult = showsService.search(show, 1, 1);
        userTorrent = searchResult.getEpisodes().iterator().next();
        assertEquals(DownloadStatus.SCHEDULED, userTorrent.getDownloadStatus());
        assertNull(userTorrent.getDownloadDate());

        reporter.info("Download the same episode again");
        showsService.downloadEpisode(userTorrent.getTorrentId());

        reporter.info("Check torrent marked as scheduled with updated download date");
        searchResult = showsService.search(show, 1, 1);
        UserTorrentJSON userTorrent2 = searchResult.getEpisodes().iterator().next();
        assertEquals(DownloadStatus.SCHEDULED, userTorrent2.getDownloadStatus());

        reporter.info("Remove search status and verify its deleted");
        showsService.removeSearchStatus(heavySearchId);
        List<SearchResultJSON> searchStatus = showsService.getSearchStatus();
        assertEquals(0, searchStatus.size());
    }

    @Test
    public void testDownloadEpisode_searchWithForceDownload() {
        reporter.info("Prepare data");
        UserData adminUser = userService.createAdminUser();
        userService.login(adminUser);
        TheTvDbShow theTvDbShow = theTvDbShowBuilder.aRunningShow().build();
        adminService.runDownloadShowsScheduleJob();
        ShowJSON show = showsService.getShow(theTvDbShow.getName());
        assertNotNull(show);
        UserData user = userService.createUser();
        userService.login(user);

        reporter.info("Search with forceDownload with non admin user");
        try {
            showsService.search(show, 1, 1, true);
            fail("forceDownload should be allowed only for admin user");
        } catch (Exception e) {
        }
        userService.login(adminUser);

        reporter.info("Add an episode and a torrent for the episode to be searched next and appear in progress");
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow, 1, 1).build();
        testPagesService.addTorrent(show, 1, 1);
        adminService.runDownloadShowsScheduleJob();

        reporter.info("Search and find episodes");
        SearchResultJSON searchResult = showsService.search(show, 1, 1);
        assertEquals(0, searchResult.getEpisodesCount());
        assertNotNull(searchResult.getStart());
        assertNull(searchResult.getEnd());
        searchResult = showsService.getSearchStatusSingleWithPolling();
        assertEquals(1, searchResult.getEpisodesCount());

        reporter.info("Search again and get result immediate from db");
        searchResult = showsService.search(show, 1, 1);
        assertEquals(1, searchResult.getEpisodesCount());

        reporter.info("Search with force download and get more results this time");
        searchResult = showsService.search(show, 1, 1, true);
        assertEquals(0, searchResult.getEpisodesCount());
        assertNotNull(searchResult.getStart());
        assertNull(searchResult.getEnd());
        WaitUtil.waitFor(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<SearchResultJSON> searchStatus = showsService.getSearchStatus();
                assertEquals(2, searchStatus.size());
                return null;
            }
        });
    }

    @Test
    public void testDownloadAllEpisodes() {
        reporter.info("Prepare data");
        UserData adminUser = userService.createAdminUser();
        userService.login(adminUser);
        TheTvDbShow theTvDbShow = theTvDbShowBuilder.aRunningShow().build();
        // setting diff air dates to prevent search of double episodes
        adminService.runDownloadShowsScheduleJob();
        ShowJSON show = showsService.getShow(theTvDbShow.getName());
        assertNotNull(show);

        // setting diff air dates to prevent search of double episodes
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow, 1, 1).withAirDate(getDate(-2)).build();
        theTvDbEpisodeBuilder.anEpisode(theTvDbShow, 1, 2).withAirDate(getDate(-1)).build();
        testPagesService.addTorrent(show, 1, 1);
        testPagesService.addTorrent(show, 1, 2);
        adminService.runDownloadShowsScheduleJob();

        UserData user = userService.createUser();
        userService.login(user);

        reporter.info("Search and get in progress status");
        SearchResultJSON searchResult = showsService.search(show, 1, -1);
        assertEquals(0, searchResult.getEpisodesCount());
        assertNotNull(searchResult.getStart());
        assertNull(searchResult.getEnd());

        reporter.info("Get status of the download");
        searchResult = showsService.getSearchStatusSingleWithPolling();
        assertEquals(2, searchResult.getEpisodesCount());

        reporter.info("Check torrent was never downloaded");
        for (UserTorrentJSON userTorrent : searchResult.getEpisodes()) {
            assertEquals(DownloadStatus.NONE, userTorrent.getDownloadStatus());
            assertNull(userTorrent.getDownloadDate());
        }

        reporter.info("Download episode for the first time");
        showsService.downloadEpisodes(Collections2.transform(searchResult.getEpisodes(), new Function<UserTorrentJSON, String>() {
            @Override
            public String apply(UserTorrentJSON userTorrentJSON) {
                return String.valueOf(userTorrentJSON.getTorrentId());
            }
        }));

        reporter.info("Check torrent marked as scheduled");
        searchResult = showsService.search(show, 1, -1);
        for (UserTorrentJSON userTorrent : searchResult.getEpisodes()) {
            assertEquals(DownloadStatus.SCHEDULED, userTorrent.getDownloadStatus());
            assertNull(userTorrent.getDownloadDate());
        }
    }

    // todo: /shows/delete/{showId}
    // /shows/autocomplete admin
    //downloadSchedule/{showId}

    private Date getDate(int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DAY_OF_YEAR, days);
        return c.getTime();
    }
}
