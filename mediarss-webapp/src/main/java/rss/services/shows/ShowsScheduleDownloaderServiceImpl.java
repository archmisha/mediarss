package rss.services.shows;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.scheduler.QuartzJob;
import rss.services.JobRunner;

/**
 * User: dikmanm
 * Date: 31/12/12 20:12
 */
@Service
@QuartzJob(name = "ShowsScheduleDownloaderJob", cronExp = "0 0 1/6 * * ?")
public class ShowsScheduleDownloaderServiceImpl extends JobRunner implements ShowsScheduleDownloaderService {

    @Autowired
    private ShowService showService;

    public ShowsScheduleDownloaderServiceImpl() {
        super(JOB_NAME);
    }

    protected String run() {
        DownloadScheduleResult downloadScheduleResult = showService.downloadLatestScheduleWithTorrents();

        if (downloadScheduleResult.getFailedShows().isEmpty()) {
            return null;
        }
        return "Failed for shows: " + StringUtils.join(downloadScheduleResult.getFailedShows(), ", ");
    }
}
