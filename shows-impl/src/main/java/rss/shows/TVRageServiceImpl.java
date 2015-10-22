package rss.shows;

import com.thoughtworks.xstream.XStream;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.MediaRSSException;
import rss.PageDownloader;
import rss.RecoverableConnectionException;
import rss.log.LogService;
import rss.shows.dao.EpisodeImpl;
import rss.shows.dao.ShowImpl;
import rss.shows.tvrage.*;
import rss.torrents.Episode;
import rss.torrents.Show;
import rss.util.DurationMeter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * User: dikmanm
 * Date: 25/02/13 09:05
 */
@SuppressWarnings("unchecked")
//@Service("tVRageServiceImpl")
public class TVRageServiceImpl implements ShowsProvider {

    public static final String SERVICES_HOSTNAME = "services.tvrage.com";
    public static final String SHOW_LIST_URL = "http://" + SERVICES_HOSTNAME + "/feeds/show_list.php";
    public static final String SHOW_INFO_URL = "http://" + SERVICES_HOSTNAME + "/feeds/full_show_info.php?sid=";
    public static final String SHOW_SCHEDULE_URL = "http://" + SERVICES_HOSTNAME + "/feeds/fullschedule.php?country=US&24_format=1";

    private PageDownloader pageDownloader;
    private LogService logService;

    public void setPageDownloader(PageDownloader pageDownloader) {
        this.pageDownloader = pageDownloader;
    }

    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    @Override
    public Show search(String name) {
        throw new UnsupportedOperationException("TVRageServiceImpl doesn't support search");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Collection<Episode> downloadSchedule() {
        Collection<Episode> result = new ArrayList<>();

        String page = pageDownloader.downloadPage(SHOW_SCHEDULE_URL);

        XStream xstream = new XStream();
        xstream.alias("schedule", List.class);
        xstream.alias("DAY", TVRageDay.class);
        xstream.useAttributeFor(TVRageDay.class, "attr");
        xstream.addImplicitCollection(TVRageDay.class, "times");
        xstream.alias("time", TVRageTime.class);
        xstream.addImplicitCollection(TVRageTime.class, "shows");
        xstream.alias("show", TVRageShowSchedule.class);
        xstream.useAttributeFor(TVRageShowSchedule.class, "name");

        List<TVRageDay> tvRageDays = (List<TVRageDay>) xstream.fromXML(page);

        // date format: 2013-2-25
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
        for (TVRageDay tvRageDay : tvRageDays) {
            try {
                Date airDate = sdf.parse(tvRageDay.getAttr());
                for (TVRageTime tvRageTime : tvRageDay.getTimes()) {
                    for (TVRageShowSchedule tvRageShowSchedule : tvRageTime.getShows()) {
                        Show show = new ShowImpl(tvRageShowSchedule.getName());
                        show.setTvRageId(tvRageShowSchedule.getSid());

                        String[] arr = tvRageShowSchedule.getEp().split("x");
                        // currently skipping 'S01-Special' all around the app
                        if (arr.length == 2) {
                            int season = Integer.parseInt(arr[0]);
                            int episodeNum = Integer.parseInt(arr[1]);
                            Episode episode = new EpisodeImpl(season, episodeNum);
                            episode.setAirDate(airDate);
                            episode.setShow(show);

                            result.add(episode);
                        }
                    }
                }
            } catch (ParseException e) {
                logService.warn(getClass(), "Failed parsing air date: " + tvRageDay.getAttr() + ": " + e.getMessage(), e);
            }
        }

        return result;
    }

    @Override
    public Collection<Show> downloadShowList() {
        Collection<Show> shows = new ArrayList<>();

        DurationMeter duration = new DurationMeter();
        String page = pageDownloader.downloadPage(SHOW_LIST_URL);
        duration.stop();
        logService.info(getClass(), "Downloading the show list from tvrage.com took " + duration.getDuration() + " ms");

        XStream xstream = new XStream();
        xstream.alias("shows", List.class);
        xstream.alias("show", TVRageShow.class);
        List<TVRageShow> tvRageShows = (List<TVRageShow>) xstream.fromXML(page);
        for (TVRageShow tvRageShow : tvRageShows) {
            // not skipping here TBD status, cuz in between seasons a show might go into that status and then continue
            if (tvRageShow.getStatus() == TVRageConstants.ShowListStatus.IN_DEV_STATUS) {
                continue;
            }
            Show show = new ShowImpl(tvRageShow.getName());
            show.setTvRageId(tvRageShow.getId());
            show.setEnded(tvRageShow.getStatus() != TVRageConstants.ShowListStatus.RETURNING_STATUS &&
                    tvRageShow.getStatus() != TVRageConstants.ShowListStatus.NEW_SERIES_STATUS);
            shows.add(show);
        }

        return shows;
    }

    @Override
    public Collection<Episode> downloadSchedule(Show show) {
        try {
            Collection<Episode> episodes = new ArrayList<>();
            String page = pageDownloader.downloadPage(SHOW_INFO_URL + show.getTvRageId());

            XStream xstream = new XStream();
            xstream.alias("Show", TVRageShowInfo.class);
            // suddenly they changed to showinfo... - correction: it is only when not using full_show_info
//			xstream.alias("Showinfo", TVRageShowInfo.class);
            xstream.alias("Episodelist", TVRageEpisodeList.class);
            xstream.alias("Season", TVRageSeason.class);
            xstream.alias("episode", TVRageEpisode.class);
            xstream.alias("genre", String.class);
            xstream.alias("Special", TVRageSpecial.class);
            xstream.alias("aka", String.class);
            xstream.alias("Movie", TVRageMovie.class);
            xstream.addImplicitCollection(TVRageEpisodeList.class, "seasons");
            xstream.addImplicitCollection(TVRageSeason.class, "episodes");
            xstream.addImplicitCollection(TVRageSpecial.class, "episodes");
            xstream.addImplicitCollection(TVRageMovie.class, "episodes");
            xstream.useAttributeFor(TVRageSeason.class, "no");

            TVRageShowInfo tvRageShowInfo = (TVRageShowInfo) xstream.fromXML(page);
            if (tvRageShowInfo.getStatus() != null) {
                show.setEnded(tvRageShowInfo.getStatus().contains(TVRageConstants.ShowInfoStatus.ENDED_STATUS));
            }

            if (tvRageShowInfo.getEpisodelist() == null) {
                logService.debug(getClass(), "Show '" + show.getName() + "' has no episodes in TVRage!");
            } else {
                List<TVRageSeason> tvRageSeasons = tvRageShowInfo.getEpisodelist().getSeasons();
                for (Object obj : tvRageSeasons) {
                    // implicit collection shit - skip movie and special
                    if (!(obj instanceof TVRageSeason)) {
                        continue;
                    }

                    TVRageSeason tvRageSeason = (TVRageSeason) obj;
                    for (TVRageEpisode tvRageEpisode : tvRageSeason.getEpisodes()) {
                        Episode episode = new EpisodeImpl();
                        episode.setSeason(tvRageSeason.getNo());
                        episode.setEpisode(Integer.parseInt(tvRageEpisode.getSeasonnum()));
                        // 2011-06-23
                        if (tvRageEpisode.getAirdate().equals("0000-00-00")) {
                            episode.setAirDate(null);
                        } else {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                episode.setAirDate(sdf.parse(tvRageEpisode.getAirdate()));
                            } catch (ParseException e) {
                                logService.warn(getClass(), "Failed parsing air date: " + tvRageEpisode.getAirdate() + ": " + e.getMessage(), e);
                                episode.setAirDate(null);
                            }
                        }
                        episodes.add(episode);
                    }
                }
            }
            return episodes;
        } catch (RecoverableConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new MediaRSSException("Failed to download schedule for show: " + show, e).withUserMessage("Failed to download schedule for show: " + show.getName());
        }
    }
}