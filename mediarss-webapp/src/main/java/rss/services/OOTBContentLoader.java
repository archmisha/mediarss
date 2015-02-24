package rss.services;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.MediaRSSException;
import rss.PageDownloader;
import rss.configuration.SettingsService;
import rss.dao.ShowDao;
import rss.entities.Episode;
import rss.entities.Show;
import rss.environment.Environment;
import rss.log.LogService;
import rss.services.shows.ShowService;
import rss.services.shows.ShowsCacheService;
import rss.services.shows.TVRageServiceImpl;
import rss.util.DurationMeter;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * User: dikmanm
 * Date: 25/02/13 19:17
 */
@Service
public class OOTBContentLoader {

	public static final String TVRAGE_SHOWS_LIST_2013_02_25 = "tvrage-shows-list-2013-02-25";

	//	@Autowired
//	private TVRageServiceImpl tvRageService;
//	@Qualifier("tVRageServiceImpl")
	private TVRageServiceImpl tvRageService;

	@Autowired
	private LogService logService;

	@Autowired
	private ShowDao showDao;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private ShowService showService;

	@Autowired
	private ShowsCacheService showsCacheService;

	@PostConstruct
	private void postConstruct() {
		tvRageService = new TVRageServiceImpl();
		tvRageService.setLogService(logService);
		tvRageService.setPageDownloader(new ResourcesPageDownloader());
	}

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void loadTVRageShows() {
		if ("true".equals(settingsService.getPersistentSetting(TVRAGE_SHOWS_LIST_2013_02_25))) {
			logService.info(getClass(), "OOTB content " + TVRAGE_SHOWS_LIST_2013_02_25 + " was already deployed. Skipping.");
			return;
		}

		logService.info(getClass(), "Loading ended-shows along with their episodes schedules from TVRage");
		DurationMeter durationMeter = new DurationMeter();

		int i = 0;
		int counter = 0;
		boolean shouldStop = false;
		while (!shouldStop) {
			Collection<Show> shows = tvRageService.downloadShowList();
			logService.info(getClass(), "  processing chunk of " + shows.size());
			if (shows.isEmpty()) {
				shouldStop = true;
			}

			for (final Show show : shows) {
				// filter out not ended shows
//				if (show.isEnded()) {
				Show persistedShow = showDao.findByTvRageId(show.getTvRageId());
				if (persistedShow == null) {
					persistedShow = showDao.findByName(show.getName());
				}

				if (persistedShow == null) {
					transactionTemplate.execute(new TransactionCallbackWithoutResult() {
						@Override
						protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
//							showService.saveNewShow(show);
							// had to copy paste from ShowServiceImpl in order to override the pageDownloader instance
							showDao.persist(show);
							showsCacheService.put(show);
							Collection<Episode> episodes = tvRageService.downloadSchedule(show);
							for (Episode episode : episodes) {
								showService.persistEpisodeToShow(show, episode);
							}
						}
					});
					counter++;
				} else if (persistedShow.getTvRageId() == -1) {
					final Show finalPersistedShow = persistedShow;
					transactionTemplate.execute(new TransactionCallbackWithoutResult() {
						@Override
						protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
							// update existing shows with tvrage id
							finalPersistedShow.setTvRageId(show.getTvRageId());
						}
					});
				}
//				}
				i++;
				if (i % 500 == 0) {
					DecimalFormat df = new DecimalFormat("#.##");
					logService.info(getClass(), "Scanned " + df.format((i / 34784.0) * 100.0) + "% shows so far, " + counter + " found as ended and not present yet");
				}
			}
		}

		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
				settingsService.setPersistentSetting(TVRAGE_SHOWS_LIST_2013_02_25, "true");
			}
		});

		durationMeter.stop();
		logService.info(getClass(), "Loaded " + counter + " ended-shows from TVRage. Time took: " + durationMeter.getDuration() + " ms");
	}

	private class ResourcesPageDownloader implements PageDownloader {

		public static final int CHUNK_SIZE = 1000;
		private int curChunkShowId = 1;
		private String showsListPage = null;

		@Override
		public String downloadPageUntilFound(String url, Pattern pattern) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String downloadPage(String url) {
			try {
				if (url.startsWith(TVRageServiceImpl.SHOW_LIST_URL)) {
					if (showsListPage == null) {
						showsListPage = IOUtils.toString(getFileInputStream("ootb" + File.separator + "tvrage-shows-list" + File.separator + "tvrage-shows-list-2013-02-25.xml"), "UTF-8");
					}

					StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
														 "<shows>");
					int idx = showsListPage.indexOf("<show><id>" + curChunkShowId);
					if (idx == -1) {
						sb.append("</shows>");
						return sb.toString();
					}
					int idx2 = showsListPage.indexOf("<show><id>" + (curChunkShowId + CHUNK_SIZE));
					if (idx2 == -1) {
						idx2 = showsListPage.length() - "</shows>".length();
					} else {
						idx2 = showsListPage.indexOf("</show>", idx2) + "</show>".length();
					}

					sb.append(showsListPage.substring(idx, idx2));
					sb.append("</shows>");
					curChunkShowId += CHUNK_SIZE;
					return sb.toString();
				} else if (url.startsWith(TVRageServiceImpl.SHOW_INFO_URL)) {
					String str = url.substring(url.indexOf('=') + 1, url.length());
					int tvRageShowId = Integer.parseInt(str);

                    File file = new File(Environment.getInstance().getAlternativeResourcesPath() + File.separator +
                            "ootb" + File.separator + "tvrage-shows-info" + File.separator + "tvrage-shows-info-2013-02-25.zip");
					ZipFile zip = new ZipFile(file);

					ZipEntry entry = zip.getEntry(tvRageShowId + ".xml");
					return IOUtils.toString(zip.getInputStream(entry), "UTF-8");
				}
			} catch (IOException e) {
				logService.error(getClass(), e.getMessage(), e);
				return null;
			}

			return null;
		}

		@Override
		public byte[] downloadData(String url) {
			throw new UnsupportedOperationException();
		}

		private InputStream getFileInputStream(String path) throws IOException {
			ClassPathResource classPathResource = new ClassPathResource(path, OOTBContentLoader.class.getClassLoader());
			if (classPathResource.exists()) {
				return classPathResource.getInputStream();
			}

            String str = Environment.getInstance().getAlternativeResourcesPath() + File.separator + path;
            File file = new File(str);
			if (file.exists()) {
				return new FileInputStream(file);
			}

			throw new MediaRSSException("Path '" + path + "' was not found in classpath nor in alternative resources path: '" +
                    Environment.getInstance().getAlternativeResourcesPath() + "'");
        }

		@Override
		public String downloadPage(String url, Map<String, String> headers) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Pair<String, String> downloadPageWithRedirect(String url) {
			throw new UnsupportedOperationException();
		}

		@Override
        public String sendPostRequest(String url, String body) {
            throw new UnsupportedOperationException();
		}
	}
}
