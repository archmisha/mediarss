package rss.services.subtitles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.services.JobRunner;
import rss.util.QuartzJob;

/**
 * User: dikmanm
 * Date: 31/12/12 20:12
 */
@Service
@QuartzJob(name = "SubtitlesDownloadScheduleJob", cronExp = "0 0 2/6 * * ?")
public class SubtitlesDownloadScheduleServiceImpl extends JobRunner implements SubtitlesDownloadScheduleService {

	@Autowired
	private SubtitlesService subtitlesService;

	public SubtitlesDownloadScheduleServiceImpl() {
		super(JOB_NAME);
	}

	protected String run() {
		subtitlesService.downloadMissingSubtitles();
		return null;
	}
}
