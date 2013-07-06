package rss.services.subtitles;

import com.turn.ttorrent.tracker.EmbeddedTracker;
import com.turn.ttorrent.tracker.TrackedTorrent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.dao.SubtitlesDao;
import rss.entities.Subtitles;
import rss.services.SettingsService;
import rss.services.log.LogService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: dikmanm
 * Date: 11/05/13 15:16
 */
@Service
public class SubtitlesTrackerServiceImpl implements SubtitlesTrackerService {

	@Autowired
	private SubtitlesDao subtitlesDao;

	@Autowired
	private LogService log;

	@Autowired
	private EmbeddedTracker embeddedTracker;

	@Autowired
	private SettingsService settingsService;

	@PostConstruct
	private void postConstruct() {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					// announce all existing subtitles
					List<Subtitles> subtitlesList = subtitlesDao.findAll();
					log.info(getClass(), "Loading " + subtitlesList.size() + " subtitles into the tracker");
					for (Subtitles subtitles : subtitlesList) {
						announce(subtitles);
					}
				} catch (Exception e) {
					log.error(getClass(), "Failed starting torrent tracker: " + e.getMessage(), e);
				}
			}
		});
		executorService.shutdown();
	}

	@Override
	public void announce(Subtitles subtitles) {
		try {
			// create and save the subtitle file for rtorrent client
			com.turn.ttorrent.common.Torrent torrent = toTorrent(subtitles);

			// save the torrent file for the rtorrent client
			File file = new File(settingsService.getTorrentWatchPath() + File.separator + torrent.getName() + ".torrent");
			try (FileOutputStream fos = new FileOutputStream(file)) {
				torrent.save(fos);
			}

			// register the torrent with the tracker
			embeddedTracker.announce(new TrackedTorrent(torrent));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public String getTrackerAnnounceUrl() {
		return embeddedTracker.getAnnounceUrl().toString();
	}

	@Override
	public com.turn.ttorrent.common.Torrent toTorrent(Subtitles subtitle) {
		try {
			File file = new File(settingsService.getTorrentDownloadedPath() + File.separator +
								 subtitle.getName() + "-" + subtitle.getLanguage().name() +
								 subtitle.getName().substring(subtitle.getName().lastIndexOf(".")));
			if (!file.exists()) {
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(subtitle.getData());
				fos.close();
			}

			com.turn.ttorrent.common.Torrent torrent = com.turn.ttorrent.common.Torrent.create(file, embeddedTracker.getAnnounceUrl().toURI(), "media-rss");
			return torrent;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
