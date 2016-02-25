package rss.subtitles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.scheduler.ScheduledJob;

/**
 * User: dikmanm
 * Date: 31/12/12 20:12
 */
@Service
public class SubtitlesDownloadScheduleServiceImpl implements ScheduledJob {

    @Autowired
    private SubtitlesService subtitlesService;

    @Override
    public String getName() {
        return "SubtitlesDownloadScheduleJob";
    }

    @Override
    public String getCronExp() {
        return "0 0 2/6 * * ?";
    }

    @Override
    public void run() {
        subtitlesService.downloadMissingSubtitles();
    }
}
