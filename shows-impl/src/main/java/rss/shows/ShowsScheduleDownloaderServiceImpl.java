package rss.services.shows;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.scheduler.ScheduledJob;
import rss.shows.DownloadScheduleResult;
import rss.shows.ShowServiceInternal;

/**
 * User: dikmanm
 * Date: 31/12/12 20:12
 */
@Service
public class ShowsScheduleDownloaderServiceImpl implements ScheduledJob {

    @Autowired
    private ShowServiceInternal showService;

    @Override
    public String getName() {
        return "ShowsScheduleDownloaderJob";
    }

    @Override
    public String getCronExp() {
        return "0 0 1/6 * * ?";
    }

    public void run() {
        DownloadScheduleResult downloadScheduleResult = showService.downloadLatestScheduleWithTorrents();

        if (!downloadScheduleResult.getFailedShows().isEmpty()) {
            throw new RuntimeException("Failed for shows: " + StringUtils.join(downloadScheduleResult.getFailedShows(), ", "));
        }
    }
}
