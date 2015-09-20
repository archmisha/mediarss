package rss.test.tests;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import rss.shows.ShowAutoCompleteJSON;
import rss.shows.ShowJSON;
import rss.shows.ShowsDaySchedule;
import rss.shows.ShowsScheduleJSON;
import rss.shows.tvrage.TVRageShow;
import rss.shows.tvrage.TVRageShowInfo;
import rss.test.entities.UserData;
import rss.test.services.AdminService;
import rss.test.services.TestPagesService;
import rss.test.shows.*;
import rss.util.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    private TVRageShowBuilder tvRageShowBuilder;

    @Autowired
    private TVRageShowInfoBuilder tvRageShowInfoBuilder;

    @Autowired
    private TVRageSeasonBuilder tvRageSeasonBuilder;

    @Autowired
    private TVRageEpisodeBuilder tvRageEpisodeBuilder;

    @Autowired
    private AdminService adminService;

    @Test
    public void testTrackedShows() {
        reporter.info("Prepare data");
        UserData adminUser = userService.createAdminUser();
        userService.login(adminUser);
        reporter.info("Create 2 shows, 1 ended and 1 not");
        TVRageShow notEndedTVRageShow = tvRageShowBuilder.aRunningShow().build();
        TVRageShow endedTVRageShow = tvRageShowBuilder.anEndedShow().build();
        adminService.runDownloadShowListJob();

        ShowJSON nonEndedShow = showsService.getShow(notEndedTVRageShow.getName());
        ShowJSON endedShow = showsService.getShow(endedTVRageShow.getName());
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
        testPagesService.setShowEnded(nonEndedShow);
        adminService.runDownloadShowListJob();

        reporter.info("Get tracked shows and verify not ended show is now ended");
        shows = showsService.getTrackedShows();
        assertEquals(1, shows.size());
        assertEquals(true, shows.get(0).isEnded());
    }

    @Test
    public void testSchedule() {
        reporter.info("Prepare data");
        UserData adminUser = userService.createAdminUser();
        userService.login(adminUser);
        TVRageShow tvRageShow1 = tvRageShowBuilder.aRunningShow().withName("show1").build();
        TVRageShow tvRageShow2 = tvRageShowBuilder.aRunningShow().withName("show2").build();
        TVRageShowInfo tvRageShowInfo1 = tvRageShowInfoBuilder.anInfo(tvRageShow1)
                .withEpisodes(tvRageSeasonBuilder.aSeason(1)
                        .withEpisodes(
                                tvRageEpisodeBuilder.anEpisode(1).withAirDate(getDate(-20)).build(), // should not return
                                tvRageEpisodeBuilder.anEpisode(2).withAirDate(getDate(-12)).build(),
                                tvRageEpisodeBuilder.anEpisode(3).withAirDate(getDate(-11)).build(),
                                tvRageEpisodeBuilder.anEpisode(4).withAirDate(getDate(-10)).build(),
                                tvRageEpisodeBuilder.anEpisode(5).withAirDate(getDate(-10)).build(),
                                tvRageEpisodeBuilder.anEpisode(6).withAirDate(getDate(-9)).build(),
                                tvRageEpisodeBuilder.anEpisode(7).withAirDate(getDate(-8)).build(),
                                tvRageEpisodeBuilder.anEpisode(8).withAirDate(getDate(-1)).build(),
                                tvRageEpisodeBuilder.anEpisode(9).withAirDate(getDate(0)).build()
                        ).build())
                .build();
        TVRageShowInfo tvRageShowInfo2 = tvRageShowInfoBuilder.anInfo(tvRageShow2)
                .withEpisodes(tvRageSeasonBuilder.aSeason(2)
                        .withEpisodes(
                                tvRageEpisodeBuilder.anEpisode(1).withAirDate(getDate(-1)).build(),
                                tvRageEpisodeBuilder.anEpisode(2).withAirDate(getDate(0)).build(),
                                tvRageEpisodeBuilder.anEpisode(3).withAirDate(getDate(+1)).build(),
                                tvRageEpisodeBuilder.anEpisode(4).withAirDate(getDate(+1)).build(),
                                tvRageEpisodeBuilder.anEpisode(5).withAirDate(getDate(+1)).build(),
                                tvRageEpisodeBuilder.anEpisode(6).withAirDate(getDate(+8)).build(),
                                tvRageEpisodeBuilder.anEpisode(7).withAirDate(getDate(+9)).build(),
                                tvRageEpisodeBuilder.anEpisode(8).withAirDate(getDate(+10)).build(),
                                tvRageEpisodeBuilder.anEpisode(9).withAirDate(getDate(+11)).build(),
                                tvRageEpisodeBuilder.anEpisode(10).withAirDate(getDate(+12)).build() // should not return
                        ).build())
                .build();
        adminService.runDownloadShowListJob();
        ShowJSON show1 = showsService.getShow(tvRageShow1.getName());
        ShowJSON show2 = showsService.getShow(tvRageShow2.getName());
        assertNotNull(show1);
        assertNotNull(show2);

        reporter.info("Get schedule for user and verify its empty");
        ShowsScheduleJSON schedule = showsService.getSchedule();
        assertEquals(1, schedule.getSchedules().size());
        assertTrue(DateUtils.isSameDay(System.currentTimeMillis(), schedule.getSchedules().get(0).getDate()));
        assertEquals(0, schedule.getSchedules().get(0).getShows().size());

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
        assertEquals(tvRageShow1.getName(), curDaySchedule.getShows().get(0).getShowName());
        assertEquals("S01E09", curDaySchedule.getShows().get(0).getSequence());
        assertEquals(tvRageShow2.getName(), curDaySchedule.getShows().get(1).getShowName());
        assertEquals("S02E02", curDaySchedule.getShows().get(1).getSequence());

        // multiple episodes of the same show on the same day, sorted by episode
        ShowsDaySchedule nextDaySchedule = schedule.getSchedules().get(7);
        assertTrue(DateUtils.isSameDay(getDate(+1).getTime(), nextDaySchedule.getDate()));
        assertEquals(3, nextDaySchedule.getShows().size());
        assertEquals(tvRageShow2.getName(), nextDaySchedule.getShows().get(0).getShowName());
        assertEquals("S02E03", nextDaySchedule.getShows().get(0).getSequence());
        assertEquals(tvRageShow2.getName(), nextDaySchedule.getShows().get(1).getShowName());
        assertEquals("S02E04", nextDaySchedule.getShows().get(1).getSequence());
        assertEquals(tvRageShow2.getName(), nextDaySchedule.getShows().get(2).getShowName());
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
        TVRageShow trackedByUserTVRageShow = tvRageShowBuilder.aRunningShow().withName(unique + "show1").build();
        TVRageShow endedTVRageShow = tvRageShowBuilder.anEndedShow().withName(unique + "show3").build();
        TVRageShow notTracked1TVRageShow = tvRageShowBuilder.aRunningShow().withName(unique + "show2").build();
        TVRageShow notTracked2TVRageShow = tvRageShowBuilder.aRunningShow().withName(unique + "sho").build();
        TVRageShow notTracked3TVRageShow = tvRageShowBuilder.aRunningShow().withName(unique + "shot").build();
        adminService.runDownloadShowListJob();
        ShowJSON trackedByUser = showsService.getShow(trackedByUserTVRageShow.getName());
        ShowJSON ended = showsService.getShow(endedTVRageShow.getName());
        ShowJSON notTracked1 = showsService.getShow(notTracked1TVRageShow.getName());
        ShowJSON notTracked2 = showsService.getShow(notTracked2TVRageShow.getName());
        ShowJSON notTracked3 = showsService.getShow(notTracked3TVRageShow.getName());
        assertNotNull(trackedByUser);
        assertNotNull(ended);
        assertNotNull(notTracked1);
        assertNotNull(notTracked2);
        assertNotNull(notTracked3);
        showsService.addTrackedShow(trackedByUser);

        reporter.info("Test auto-complete not returns shows already tracked by user and ended shows");
        ShowAutoCompleteJSON showAutoCompleteJSON = showsService.autoCompleteTracked(unique + "show");
        assertEquals(1, showAutoCompleteJSON.getTotal());
        assertEquals(notTracked1TVRageShow.getName(), showAutoCompleteJSON.getShows().get(0).getText());

        reporter.info("Test auto-complete returns multiple items sorted by name");
        showAutoCompleteJSON = showsService.autoCompleteTracked(unique + "sho");
        assertEquals(3, showAutoCompleteJSON.getTotal());
        assertEquals(notTracked2TVRageShow.getName(), showAutoCompleteJSON.getShows().get(0).getText());
        assertEquals(notTracked3TVRageShow.getName(), showAutoCompleteJSON.getShows().get(1).getText());
        assertEquals(notTracked1TVRageShow.getName(), showAutoCompleteJSON.getShows().get(2).getText());

        reporter.info("Test auto-complete returns no results");
        showAutoCompleteJSON = showsService.autoCompleteTracked(unique + "shoz");
        assertEquals(0, showAutoCompleteJSON.getTotal());
        assertEquals(0, showAutoCompleteJSON.getShows().size());
    }

    //search
    ///search/status
    ///search/remove/{searchId}
    // episodeDownload
    //episodeDownloadAll

    private Date getDate(int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DAY_OF_YEAR, days);
        return c.getTime();
    }
}
