package rss.services.shows;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.dao.EpisodeDao;
import rss.entities.Episode;
import rss.entities.MediaQuality;
import rss.entities.Show;
import rss.services.EmailService;
import rss.services.EpisodeRequest;
import rss.services.JobRunner;
import rss.services.downloader.DownloadResult;
import rss.services.downloader.TVShowsTorrentEntriesDownloader;
import rss.util.DateUtils;
import rss.util.QuartzJob;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * User: dikmanm
 * Date: 31/12/12 20:12
 */
@Service
@QuartzJob(name = "ShowsScheduleDownloaderJob", cronExp = "0 30 0/12 * * ?")
public class ShowsScheduleDownloaderServiceImpl extends JobRunner implements ShowsScheduleDownloaderService {

	@Autowired
	private ShowService showService;

	@Autowired
	private EpisodeDao episodeDao;

	@Autowired
	private EmailService emailService;

	@Autowired
	private TVShowsTorrentEntriesDownloader torrentEntriesDownloader;

	public ShowsScheduleDownloaderServiceImpl() {
		super(JOB_NAME);
	}

	protected String run() {
		final Set<EpisodeRequest> missing = new HashSet<>();
		final Collection<Show> failedShows = new ArrayList<>();

		// must separate schedule download and torrent download into separate transactions
		// cuz in the first creating episodes which must be available (committed) in the second part
		// and the second part spawns separate threads and transactions
		final Set<EpisodeRequest> episodesToDownload = transactionTemplate.execute(new TransactionCallback<Set<EpisodeRequest>>() {
			@Override
			public Set<EpisodeRequest> doInTransaction(TransactionStatus arg0) {
				DownloadScheduleResult downloadScheduleResult = showService.downloadSchedule();
				failedShows.addAll(downloadScheduleResult.getFailedShows());

				// download torrents for the new episodes
				final Set<EpisodeRequest> episodesToDownload = new HashSet<>();
				for (Episode episode : downloadScheduleResult.getNewEpisodes()) {
					// todo: skip episodes without air date set ??? do need ??
					if (!episode.isUnAired()) {
						// to speed up things download only for the past 14 days
						Calendar c = Calendar.getInstance();
						c.add(Calendar.DAY_OF_MONTH, -(14 + 1));
						if (episode.getAirDate() == null || episode.getAirDate().after(c.getTime())) {
							Show show = episode.getShow();
							episodesToDownload.add(new EpisodeRequest(show.getName(), show, MediaQuality.HD720P, episode.getSeason(), episode.getEpisode()));
							logService.info(getClass(), "Will try to download torrents of " + episode);
						}
					} else {
						logService.debug(getClass(), "Skipping downloading '" + episode.toString() + "' - still un-aired");
					}
				}

				return episodesToDownload;
			}
		});

		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus arg0) {
				DownloadResult<Episode, EpisodeRequest> downloadResult = torrentEntriesDownloader.download(episodesToDownload);
				missing.addAll(downloadResult.getMissing());
			}
		});

		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus arg0) {
				// if missing episode is released today (no matter the hour) then don't email it
				for (EpisodeRequest episodeRequest : new ArrayList<>(missing)) {
					Episode episode = episodeDao.find(episodeRequest);
					// might be null cuz maybe there is no such episode at al - who knows what they search for
					if (episode != null && episode.getAirDate() != null && DateUtils.isToday(episode.getAirDate())) {
						missing.remove(episodeRequest);
					}
				}
				emailService.notifyOfMissingEpisodes(missing);
			}
		});

		if (failedShows.isEmpty()) {
			return null;
		}
		return "Failed for shows: " + StringUtils.join(failedShows, ", ");
	}
}
