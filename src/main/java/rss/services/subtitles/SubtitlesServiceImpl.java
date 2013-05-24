package rss.services.subtitles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.dao.SubtitlesDao;
import rss.entities.Subtitles;
import rss.services.EmailService;
import rss.services.SettingsService;
import rss.services.downloader.DownloadResult;
import rss.services.downloader.SubtitlesDownloader;
import rss.services.log.LogService;
import rss.services.requests.subtitles.SubtitlesRequest;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: dikmanm
 * Date: 24/01/13 22:39
 */
@Service
public class SubtitlesServiceImpl implements SubtitlesService {

	@Autowired
	private SubtitlesDao subtitlesDao;

	@Autowired
	private LogService logService;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private SubtitlesDownloader subtitlesDownloader;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private EmailService emailService;

//	@Override
//	@Transactional(propagation = Propagation.REQUIRED)
//	public void downloadEpisodeSubtitles(Torrent torrent, Episode episode, SubtitleLanguage language) {
//		downloadEpisodeSubtitles(torrent, episode, Collections.singleton(language));
//	}
//
//	@Override
//	@Transactional(propagation = Propagation.REQUIRED)
//	public void downloadEpisodeSubtitles(Torrent torrent, Episode episode, Collection<SubtitleLanguage> languages) {
//		try {
//			OpenSubtitlesAPI openSubtitlesAPI = new OpenSubtitlesAPI();
//			String token = openSubtitlesAPI.login("", "");
//
//			for (SubtitleLanguage language : languages) {
//				if (subtitlesDao.find(torrent, language) == null) {
//					//todo: should translate language to opensubtitles language
//
//					// fallback to english
//					List<Map<String, Object>> maps = openSubtitlesAPI.searchEpisode(token, episode.getName(), episode.getSeason(), episode.getEpisode(), new LANGUAGE[]{LANGUAGE.HEB, LANGUAGE.ENG});
//					log.info(getClass(), "found " + maps.size() + " subtitles for episode \"" + episode + "\"");
//
//					// search for the subtitles with the needed language
//					Map<String, Object> obj = null;
//					for (Map<String, Object> map : maps) {
//						SubtitleLanguage curSubLanguage = SubtitleLanguage.fromString((String) map.get("LanguageName"));
//						if (curSubLanguage == SubtitleLanguage.HEBREW) {
//							obj = map;
//						}
//					}
//					if (!maps.isEmpty() && obj == null) {
//						obj = maps.get(0);
//					}
//
//					if (obj != null) {
//						Subtitles subtitles = createSubtitles(openSubtitlesAPI, token, obj);
//						subtitles.setTorrent(torrent);
//						subtitlesDao.persist(subtitles);
//						announce(subtitles);
//						log.info(getClass(), "Downloaded subtitles for \"" + episode + "\": " + subtitles.getReleaseName() + " lang: " + subtitles.getLanguage());
//					}
//				}
//			}
//		} catch (OpenSubtitlesException e) {
//			throw new RuntimeException(e.getMessage(), e);
//		}
//	}

	@Override
	public void downloadMissingSubtitles() {
		// this is the place that looks for subtitles for media that are not found before
		// do need this?
		// get all tracked shows of users
		// todo
	}

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void downloadSubtitlesAsync(final List<SubtitlesRequest> subtitlesRequests) {
		if (!settingsService.areSubtitlesEnabled()) {
			return;
		}

		final Class clazz = getClass();
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					transactionTemplate.execute(new TransactionCallbackWithoutResult() {
						@Override
						protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
							/*List<SubtitleLanguage> subtitlesLanguages = subtitlesDao.getSubtitlesLanguages();
							if (subtitlesLanguages.isEmpty()) {
								return;
							}
							for (SubtitlesRequest subtitlesRequest : subtitlesRequests) {
								subtitlesRequest.getLanguages().addAll(subtitlesLanguages);
							}*/
							DownloadResult<Subtitles,SubtitlesRequest> downloadResult = subtitlesDownloader.download(subtitlesRequests);
							emailService.notifyOfMissingSubtitles(downloadResult.getMissing());
						}
					});
				} catch (Exception e) {
					logService.error(clazz, e.getMessage(), e);
				}
			}
		});
		executorService.shutdown();
	}

//	private Subtitles createSubtitles(OpenSubtitlesAPI openSubtitlesAPI, String token, Map<String, Object> obj) throws OpenSubtitlesException {
//		String releaseName = (String) obj.get("MovieReleaseName");
//		String subId = (String) obj.get("IDSubtitleFile");
//		int subIdInt = Integer.parseInt(subId);
//		SubtitleLanguage subLanguage = SubtitleLanguage.fromString((String) obj.get("LanguageName"));
//		String subFileName = (String) obj.get("SubFileName");
//
//		Map<Integer, byte[]> data = openSubtitlesAPI.download(token, subIdInt);
//
//		Subtitles subtitles = new Subtitles();
//		subtitles.setData(data.get(subIdInt));
//		subtitles.setReleaseName(releaseName);
//		subtitles.setExternalId(subId);
//		subtitles.setLanguage(subLanguage);
//		subtitles.setFileName(subFileName);
//		return subtitles;
//	}
}
