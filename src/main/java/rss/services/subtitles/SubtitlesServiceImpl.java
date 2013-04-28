package rss.services.subtitles;

import com.googlecode.opensubtitlesjapi.LANGUAGE;
import com.googlecode.opensubtitlesjapi.OpenSubtitlesAPI;
import com.googlecode.opensubtitlesjapi.OpenSubtitlesException;
import com.turn.ttorrent.tracker.EmbeddedTracker;
import com.turn.ttorrent.tracker.TrackedTorrent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.dao.SubtitlesDao;
import rss.entities.Subtitles;
import rss.services.SettingsService;
import rss.services.log.LogService;
import rss.entities.Episode;
import rss.entities.Torrent;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 24/01/13 22:39
 */
@Service
public class SubtitlesServiceImpl implements SubtitlesService {

	@Autowired
	private SubtitlesDao subtitlesDao;

	@Autowired
	private LogService log;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private EmbeddedTracker embeddedTracker;

	@PostConstruct
	private void postConstruct() {
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

	private void announce(Subtitles subtitles) {
		try {
			// create and save the subtitle file for rtorrent client
			com.turn.ttorrent.common.Torrent torrent = toTorrent(subtitles);

			// save the torrent file for the rtorrent client
			File file = new File(settingsService.getTorrentWatchPath() + File.separator + torrent.getName() + ".torrent");
			FileOutputStream fos = new FileOutputStream(file);
			torrent.save(fos);
			fos.close();

			// register the torrent with the tracker
			embeddedTracker.announce(new TrackedTorrent(torrent));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void downloadTorrentSubtitles(Torrent torrent, SubtitleLanguage language) {
		downloadTorrentSubtitles(torrent, Collections.singleton(language));
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void downloadTorrentSubtitles(Torrent torrent, Collection<SubtitleLanguage> languages) {
		/*try {
			OpenSubtitlesAPI openSubtitlesAPI = new OpenSubtitlesAPI();
			String token = openSubtitlesAPI.login();
			for (SubtitleLanguage language : languages) {
				if (subtitlesDao.find(torrent, language) == null) {
					List<Map<String,Object>> maps = openSubtitlesAPI.searchEpisode(token, "", 1, 1, new LANGUAGE[]{LANGUAGE.HEB});
				}
			}
		} catch (OpenSubtitlesException e) {
			throw new RuntimeException(e.getMessage(), e);
		}*/
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void downloadEpisodeSubtitles(Torrent torrent, Episode episode, SubtitleLanguage language) {
		downloadEpisodeSubtitles(torrent, episode, Collections.singleton(language));
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void downloadEpisodeSubtitles(Torrent torrent, Episode episode, Collection<SubtitleLanguage> languages) {
		try {
			OpenSubtitlesAPI openSubtitlesAPI = new OpenSubtitlesAPI();
			String token = openSubtitlesAPI.login("", "");

			for (SubtitleLanguage language : languages) {
				if (subtitlesDao.find(torrent, language) == null) {
					//todo: should translate language to opensubtitles language

					// fallback to english
					List<Map<String, Object>> maps = openSubtitlesAPI.searchEpisode(token, episode.getName(), episode.getSeason(), episode.getEpisode(), new LANGUAGE[]{LANGUAGE.HEB, LANGUAGE.ENG});
					log.info(getClass(), "found " + maps.size() + " subtitles for episode \"" + episode + "\"");

					// search for the subtitles with the needed language
					Map<String, Object> obj = null;
					for (Map<String, Object> map : maps) {
						SubtitleLanguage curSubLanguage = SubtitleLanguage.fromString((String) map.get("LanguageName"));
						if (curSubLanguage == SubtitleLanguage.HEBREW) {
							obj = map;
						}
					}
					if (!maps.isEmpty() && obj == null) {
						obj = maps.get(0);
					}

					if (obj != null) {
						Subtitles subtitles = createSubtitles(openSubtitlesAPI, token, obj);
						subtitles.setTorrent(torrent);
						subtitlesDao.persist(subtitles);
						announce(subtitles);
						log.info(getClass(), "Downloaded subtitles for \"" + episode + "\": " + subtitles.getReleaseName() + " lang: " + subtitles.getLanguage());
					}
				}
			}
		} catch (OpenSubtitlesException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public com.turn.ttorrent.common.Torrent toTorrent(Subtitles subtitle) {
		try {
			File file = new File(settingsService.getTorrentDownloadedPath() + File.separator +
								 subtitle.getTorrent().getTitle() + "-" + subtitle.getLanguage() +
								 subtitle.getFileName().substring(subtitle.getFileName().lastIndexOf(".")));
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

	@Override
	public String getTrackerAnnounceUrl() {
		return embeddedTracker.getAnnounceUrl().toString();
	}

	private Subtitles createSubtitles(OpenSubtitlesAPI openSubtitlesAPI, String token, Map<String, Object> obj) throws OpenSubtitlesException {
		String releaseName = (String) obj.get("MovieReleaseName");
		String subId = (String) obj.get("IDSubtitleFile");
		int subIdInt = Integer.parseInt(subId);
		SubtitleLanguage subLanguage = SubtitleLanguage.fromString((String) obj.get("LanguageName"));
		String subFileName = (String) obj.get("SubFileName");

		Map<Integer, byte[]> data = openSubtitlesAPI.download(token, subIdInt);

		Subtitles subtitles = new Subtitles();
		subtitles.setData(data.get(subIdInt));
		subtitles.setReleaseName(releaseName);
		subtitles.setExternalId(subId);
		subtitles.setLanguage(subLanguage);
		subtitles.setFileName(subFileName);
		return subtitles;
	}
}
