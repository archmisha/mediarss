package rss.shows.providers;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.MediaRSSException;
import rss.PageDownloader;
import rss.log.LogService;
import rss.rms.ResourceManagementService;
import rss.shows.dao.EpisodeImpl;
import rss.shows.dao.ShowDao;
import rss.shows.dao.ShowImpl;
import rss.shows.thetvdb.TheTvDbConstants;
import rss.shows.thetvdb.TheTvDbEpisode;
import rss.shows.thetvdb.TheTvDbShow;
import rss.shows.thetvdb.TheTvDbSyncTime;
import rss.torrents.Episode;
import rss.torrents.Show;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * User: dikmanm
 * Date: 16/10/2015 08:39
 */
@Component
public class TheTVDbServiceImpl implements ShowsProvider {

    private static final String API_KEY = "EB8D0878240F2DD7";
    private static final String HOST = "http://thetvdb.com";
    public static final String SEARCH_URL = HOST + "/api/GetSeries.php?seriesname=";
    public static final String SERVER_TIME_URL = HOST + "/api/Updates.php?type=none";
    public static final String SHOW_URL = HOST + "/api/" + API_KEY + "/series/%s/all/en.zip";
    public static final String UPDATE_URL = HOST + "/api/Updates.php?type=all&time=";
    public static final String EPISODE_URL = HOST + "/api/" + API_KEY + "/episodes/%s/en.xml";

    @Autowired
    private PageDownloader pageDownloader;

    @Autowired
    private ResourceManagementService rmsService;

    @Autowired
    private ShowDao showDao;

    @Autowired
    private LogService logService;

    @Override
    public Show search(String name) {
        String page = pageDownloader.downloadPage(SEARCH_URL + name);

        XStream xstream = new XStream();
        xstream.ignoreUnknownElements();
        xstream.processAnnotations(TheTvDbShow.class);
        xstream.alias("Data", List.class);
        xstream.alias("Series", TheTvDbShow.class);
        List<TheTvDbShow> series = (List<TheTvDbShow>) xstream.fromXML(page);
        if (series.isEmpty()) {
            return null;
        }
        TheTvDbShow theTvDbShow = series.get(0);
        Show show = new ShowImpl();
        show.setTheTvDbId(theTvDbShow.getId());
        show.setName(theTvDbShow.getName());
        return show;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Collection<Show> downloadShowList() {
        updateLastSyncTime();

        Collection<Show> result = new ArrayList<>();
        List<Show> shows = showDao.getShowsWithoutTheTvDbId();
        for (Show show : shows) {
            Show showResult = search(show.getName());
            if (showResult == null) {
                logService.warn(getClass(), "Didn't find show '" + show.getName() + "' in TheTVDB");
                show.setTheTvDbScanDate(new Date());
            } else {
                showResult.setId(show.getId());
                result.add(showResult);
            }
        }
        return result;
    }

    @Override
    public SyncData getSyncData() {
        SyncData result = new SyncData();

        TheTvDbSyncTime syncTime = getTheTvDbSyncTime();
        if (syncTime == null) {
            return result;
        }

        String page = pageDownloader.downloadPage(UPDATE_URL + syncTime.getTime());

        XStream xstream = new XStream();
        xstream.ignoreUnknownElements();
        xstream.processAnnotations(UpdateItem.class);
        xstream.alias("Items", UpdateItem.class);
        xstream.addImplicitCollection(UpdateItem.class, "theTvDbShowIds", "Series", Long.class);
        xstream.addImplicitCollection(UpdateItem.class, "theTvDbEpisodeIds", "Episode", Long.class);
        UpdateItem updateItem = (UpdateItem) xstream.fromXML(page);

        if (updateItem.getTime() != null) {
            syncTime.setTime(updateItem.getTime());
        }

        if (updateItem.getTheTvDbShowIds() != null) {
            for (Long theTvDbShowId : updateItem.getTheTvDbShowIds()) {
                ShowData showData = getShowData(theTvDbShowId);
                result.addShow(showData.getShow());
                result.addEpisodes(showData.getEpisodes());
            }
        }

        // avoid duplicated episodes from both show update and direct episode update. Can that even happen in the api?
        Set<Long> tvDbEpisodeIds = new HashSet<>();
        tvDbEpisodeIds.addAll(Collections2.transform(result.getEpisodes(), new Function<Episode, Long>() {
            @Override
            public Long apply(Episode episode) {
                return episode.getTheTvDbId();
            }
        }));
        if (updateItem.getTheTvDbEpisodeIds() != null) {
            for (Long theTvDbEpisodeId : updateItem.getTheTvDbEpisodeIds()) {
                if (!tvDbEpisodeIds.contains(theTvDbEpisodeId)) {
                    result.addEpisode(getEpisodeData(theTvDbEpisodeId));
                }
            }
        }

        rmsService.saveOrUpdate(syncTime, TheTvDbSyncTime.class);

        return result;
    }

    private Episode getEpisodeData(long theTvDbEpisodeId) {
        String xml = pageDownloader.downloadPage(String.format(EPISODE_URL, theTvDbEpisodeId));
        XStream xstream = new XStream();
        xstream.ignoreUnknownElements();
        xstream.processAnnotations(TheTvDbEpisode.class);
        xstream.alias("Data", TheTvDbEpisode.class);
        TheTvDbEpisode theTvDbEpisode = (TheTvDbEpisode) xstream.fromXML(xml);
        Episode episode = toEpisode(theTvDbEpisode);
        episode.setShow(showDao.findByTheTvDbId(theTvDbEpisode.getShowId()));
        return episode;
    }

    @Override
    public ShowData getShowData(Show show) {
        return getShowData(show.getTheTvDbId());
    }

    private ShowData getShowData(long theTvDbShowId) {
        byte[] zip = pageDownloader.downloadData(String.format(SHOW_URL, theTvDbShowId));
        try {
            try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zip))) {
                ZipEntry zipEntry = zipInputStream.getNextEntry();
                while (zipEntry != null && !zipEntry.getName().equals("en.xml")) {
                    zipEntry = zipInputStream.getNextEntry();
                }
                String xml = IOUtils.toString(zipInputStream);

                XStream xstream = new XStream();
                xstream.ignoreUnknownElements();
                xstream.processAnnotations(TheTvDbShow.class);
                xstream.processAnnotations(TheTvDbEpisode.class);
                xstream.alias("Data", List.class);
                xstream.alias("Series", TheTvDbShow.class);
                xstream.alias("Episode", TheTvDbEpisode.class);
                ArrayList arr = (ArrayList) xstream.fromXML(xml);
                TheTvDbShow theTvDbShow = (TheTvDbShow) arr.get(0);
                List<TheTvDbEpisode> episodes = (List<TheTvDbEpisode>) arr.subList(1, arr.size());

                Show resultShow = toShow(theTvDbShow);
                ShowData showData = new ShowData(resultShow);
                for (TheTvDbEpisode theTvDbEpisode : episodes) {
                    Episode episode = toEpisode(theTvDbEpisode);
                    episode.setShow(resultShow);
                    showData.addEpisode(episode);
                }

                return showData;
            }
        } catch (IOException e) {
            throw new MediaRSSException(e.getMessage(), e);
        }
    }

    private Show toShow(TheTvDbShow theTvDbShow) {
        Show resultShow = new ShowImpl();
        resultShow.setTheTvDbId(theTvDbShow.getId());
        resultShow.setName(theTvDbShow.getName());
        if (TheTvDbConstants.ENDED_STATUS.equals(theTvDbShow.getStatus())) { // might be null
            resultShow.setEnded(true);
        }
        return resultShow;
    }

    private Episode toEpisode(TheTvDbEpisode theTvDbEpisode) {
        Episode episode = new EpisodeImpl(theTvDbEpisode.getSeason(), theTvDbEpisode.getEpisode());
        episode.setTheTvDbId(theTvDbEpisode.getId());
        if (StringUtils.isBlank(theTvDbEpisode.getAirDate())) {
            episode.setAirDate(null);
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                episode.setAirDate(sdf.parse(theTvDbEpisode.getAirDate()));
            } catch (ParseException e) {
                logService.warn(getClass(), "Failed parsing air date: " + theTvDbEpisode.getAirDate() + ": " + e.getMessage(), e);
                episode.setAirDate(null);
            }
        }
        return episode;
    }

    private TheTvDbSyncTime getTheTvDbSyncTime() {
        return rmsService.get(rmsService.apiFactory().createGetResourceOperation(TheTvDbSyncTime.class, rmsService.apiFactory().createRmsQueryBuilder().getRmsQueryInformation()));
    }

    private void updateLastSyncTime() {
        TheTvDbSyncTime syncTime = getTheTvDbSyncTime();
        if (syncTime == null) {
            String page = pageDownloader.downloadPage(SERVER_TIME_URL);
            XStream xstream = new XStream();
            xstream.ignoreUnknownElements();
            xstream.processAnnotations(UpdateItem.class);
            xstream.alias("Items", UpdateItem.class);
            UpdateItem updateItem = (UpdateItem) xstream.fromXML(page);
            syncTime = new TheTvDbSyncTime();
            syncTime.setTime(updateItem.getTime());
            rmsService.saveOrUpdate(syncTime, TheTvDbSyncTime.class);
        }
    }

    private class UpdateItem {
        @XStreamAlias("Time")
        private Long time;

        @XStreamAlias("Series")
        private Long[] theTvDbShowIds;

        @XStreamAlias("Episode")
        private Long[] theTvDbEpisodeIds;

        public Long getTime() {
            return time;
        }

        public void setTime(Long time) {
            this.time = time;
        }

        public Long[] getTheTvDbShowIds() {
            return theTvDbShowIds;
        }

        public void setTheTvDbShowIds(Long[] theTvDbShowIds) {
            this.theTvDbShowIds = theTvDbShowIds;
        }

        public Long[] getTheTvDbEpisodeIds() {
            return theTvDbEpisodeIds;
        }

        public void setTheTvDbEpisodeIds(Long[] theTvDbEpisodeIds) {
            this.theTvDbEpisodeIds = theTvDbEpisodeIds;
        }
    }
}
