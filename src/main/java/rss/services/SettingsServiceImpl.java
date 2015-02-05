package rss.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import rss.dao.SettingsDao;
import rss.services.log.LogService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: Michael Dikman
 * Date: 13/12/12
 * Time: 21:03
 */
@Service
public class SettingsServiceImpl implements SettingsService {

	public static final String SETTINGS_FILENAME = "settings.properties";
	public static final String SETTINGS_FILE_PATH = "settings_file_path";
	public static final String ADMIN_DEFAULT_EMAIL = "archmisha@gmail.com";

	@Autowired
	private LogService logService;

	@Autowired
	private SettingsDao settingsDao;

	private Date deploymentDate;
	private Date startupDate;
	private ExecutorService executorService;
	private boolean shouldRun;
	private WatchService watchService;

	private SettingsBean settingsBean;

	private Collection<SettingsUpdateListener> updateListeners = new ArrayList<>();

	@PostConstruct
	private void postConstruct() {
		shouldRun = true;
		loadSettingsFile();

		executorService = Executors.newSingleThreadExecutor();
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					watchFile(new File(getSettingsFilePath()).getAbsolutePath(), SETTINGS_FILENAME);
				} catch (Exception e) {
					if (shouldRun) {
						logService.error(getClass(), String.format("Failed setting up file watcher for %s: %s", SETTINGS_FILENAME, e.getMessage()), e);
					}
				}
			}
		});
	}

	private String getSettingsFilePath() {
		return System.getProperty(SETTINGS_FILE_PATH, System.getProperty("user.home"));
	}

	@PreDestroy
	private void preDestroy() {
		logService.info(getClass(), "Terminating settings service and watch job");
		shouldRun = false;
		executorService.shutdown();
		try {
			watchService.close();
		} catch (IOException e) {
			logService.error(getClass(), e.getMessage(), e);
		}
	}

	private void loadSettingsFile() {
		logService.info(getClass(), "Loading " + SETTINGS_FILENAME + " file");
		try {
			File settingsFile = new File(getSettingsFilePath() + File.separator + SETTINGS_FILENAME);
			if (settingsFile.exists()) {
				logService.info(getClass(), "Loading " + SETTINGS_FILENAME + " file from " + settingsFile.getAbsolutePath());
			} else {
				logService.info(getClass(), "Custom settings file not found on path: " + settingsFile.getAbsolutePath());
				logService.info(getClass(), "Loading default " + SETTINGS_FILENAME + " file from classpath");
				settingsFile = new ClassPathResource(SETTINGS_FILENAME, SettingsServiceImpl.class.getClassLoader()).getFile();
			}
			Properties prop = new Properties();
			prop.load(new FileReader(settingsFile));
			settingsBean = new SettingsBean(prop);
		} catch (Exception e) {
			logService.error(getClass(), "Failed loading " + SETTINGS_FILENAME + ": " + e.getMessage(), e);
		}

		// call all registered listeners
		for (SettingsUpdateListener updateListener : updateListeners) {
			try {
				updateListener.onSettingsUpdated();
			} catch (Exception e) {
				logService.error(getClass(), "Failed updating settings listener: " + e.getMessage(), e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void watchFile(String watchFolderPath, String watchedFileName) throws IOException, InterruptedException {
		//create the watchService
		watchService = FileSystems.getDefault().newWatchService();

		//register the directory with the watchService for create, modify and delete events
		final Path path = Paths.get(watchFolderPath);
		path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

		long lastFileChange = System.currentTimeMillis();

		//start an infinite loop
		while (shouldRun) {

			// remove the next watch key
			final WatchKey key = watchService.take();

			// get list of events for the watch key
			for (WatchEvent<?> watchEvent : key.pollEvents()) {

				//get the kind of event (create, modify, delete)
				final WatchEvent.Kind<?> kind = watchEvent.kind();

				// This key is registered only for ENTRY_CREATE events, but an OVERFLOW event can
				// occur regardless if events are lost or discarded.
				if (kind.equals(StandardWatchEventKinds.OVERFLOW)) {
					continue;
				}

				// get the filename for the event
				final WatchEvent<Path> ev = (WatchEvent<Path>) watchEvent;
				final Path filename = ev.context();

				// print it out
//				System.out.println(kind + ": " + filename);
				// prevent firing events when occur in less than 5 ms apart from each other - due to some bug of events fired twice
				long now = System.currentTimeMillis();
				if (filename.endsWith(watchedFileName) && (now - lastFileChange) > 5) {
					lastFileChange = now;
					loadSettingsFile();
				}
			}

			// reset the key
			boolean valid = key.reset();

			// exit loop if the key is not valid e.g. if the directory was deleted
			if (!valid) {
				break;
			}
		}
	}

	@Override
	public void setDeploymentDate(Date deploymentDate) {
		this.deploymentDate = deploymentDate;
	}

	@Override
	public Date getDeploymentDate() {
		return deploymentDate;
	}

	@Override
	public void setStartupDate(Date startupDate) {
		this.startupDate = startupDate;
	}

	@Override
	public Date getStartupDate() {
		return startupDate;
	}

	@Override
	public boolean useWebProxy() {
		return settingsBean.useWebProxy();
	}

	@Override
	public String getWebHostName() {
		return settingsBean.getWebHostName();
	}

	@Override
	public boolean isDevEnvironment() {
		return settingsBean.isDevEnvironment();
	}

	@Override
	public String getWebRootContext() {
		return settingsBean.getWebRootContext();
	}

	@Override
	public String getTrackerUrl() {
		return settingsBean.getTrackerUrl();
	}

	@Override
	public String getImagesPath() {
		return settingsBean.getImagesPath();
	}

	@Override
	public boolean isLogMemory() {
		return settingsBean.isLogMemory();
	}

	@Override
	public boolean areSubtitlesEnabled() {
		return settingsBean.areSubtitlesEnabled();
	}

	@Override
	public int getWebPort() {
		return settingsBean.getWebPort();
	}

	@Override
	public String getTorrentDownloadedPath() {
		return settingsBean.getTorrentDownloadedPath();
	}

	@Override
	public int getTVComPagesToDownload() {
		return settingsBean.getTVComPagesToDownload();
	}

	@Override
	public String getAlternativeResourcesPath() {
		return settingsBean.getAlternativeResourcesPath();
	}

	@Override
	public String getTorrentWatchPath() {
		return settingsBean.getTorrentWatchPath();
	}

	@Override
	public Set<String> getAdministratorEmails() {
		return settingsBean.getAdminEmails();
	}

	@Override
	public String getShowAlias(String name) {
		return settingsBean.getShowAlias(name);
	}

	@Override
	public int getShowSeasonAlias(String name, int season) {
		return settingsBean.getShowSeasonAlias(name, season);
	}

	@Override
	public String getPersistentSetting(String key) {
		return settingsDao.getSettings(key);
	}

	@Override
	public void setPersistentSetting(String key, String value) {
		settingsDao.setSettings(key, value);
	}

	@Override
	public void addUpdateListener(SettingsUpdateListener listener) {
		this.updateListeners.add(listener);
	}

	@Override
	public void removeUpdateListener(SettingsUpdateListener listener) {
		this.updateListeners.remove(listener);
	}

	private class SettingsBean {
		private Properties prop;

		private Set<String> adminEmails;
		private boolean logMemory;
		private int tvComPagesToDownload;
		private int webPort;
		private boolean areSubtitlesEnabled;
		private boolean devEnvironment;
		private boolean useWebProxy;
		private String trackerUrl;
		private String torrentDownloadedPath;
		private String webRootContext;
		private String torrentWatchPath;
		private String alternativeResourcesPath;
		private String imagesPath;

		public SettingsBean(Properties prop) {
			this.prop = prop;

			adminEmails = new HashSet<>();
			adminEmails.add(ADMIN_DEFAULT_EMAIL);
			String admins = prop.getProperty("admins");
			if (admins != null) {
				adminEmails.addAll(Arrays.asList(admins.split(",")));
			}
			logMemory = "true".equals(prop.getProperty("log.memory"));
			tvComPagesToDownload = Integer.parseInt(prop.getProperty("tvcom.pages.to.download"));
			webPort = Integer.parseInt(prop.getProperty("web.port"));
			areSubtitlesEnabled = "true".equals(prop.getProperty("subtitles"));
			devEnvironment = getWebHostName().equals("localhost");
			useWebProxy = "true".equals(prop.getProperty("webproxy"));
			trackerUrl = prop.getProperty("tracker.url");
			torrentDownloadedPath = prop.getProperty("torrent.downloaded.path");
			webRootContext = prop.getProperty("web.root.context");
			torrentWatchPath = prop.getProperty("torrent.watch.path");
			alternativeResourcesPath = prop.getProperty("alternative.resources.path");
			imagesPath = prop.getProperty("images.path");
		}

		public Set<String> getAdminEmails() {
			return adminEmails;
		}

		public boolean isLogMemory() {
			return logMemory;
		}

		public int getTVComPagesToDownload() {
			return tvComPagesToDownload;
		}

		public int getWebPort() {
			return webPort;
		}

		public boolean areSubtitlesEnabled() {
			return areSubtitlesEnabled;
		}

		public boolean isDevEnvironment() {
			return devEnvironment;
		}

		public boolean useWebProxy() {
			return useWebProxy;
		}

		public String getTrackerUrl() {
			return trackerUrl;
		}

		public String getTorrentDownloadedPath() {
			return torrentDownloadedPath;
		}

		public String getWebRootContext() {
			return webRootContext;
		}

		public String getTorrentWatchPath() {
			return torrentWatchPath;
		}

		public String getAlternativeResourcesPath() {
			return alternativeResourcesPath;
		}

		public String getShowAlias(String name) {
			for (String key : prop.stringPropertyNames()) {
				if (key.startsWith("show.alias.") && key.endsWith(".name") && prop.getProperty(key).equalsIgnoreCase(name)) {
					String aliasKey = key.substring(0, key.lastIndexOf(".")) + ".alias";
					return prop.getProperty(aliasKey);
				}
			}
			return null;
		}

		public String getWebHostName() {
			try {
				String value = prop.getProperty("web.host");
				if (StringUtils.isBlank(value)) {
					value = InetAddress.getLocalHost().getHostAddress();
				}
				return value;
			} catch (UnknownHostException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}

		public int getShowSeasonAlias(String name, int season) {
			for (String key : prop.stringPropertyNames()) {
				if (key.startsWith("show.alias.") && key.endsWith(".name") && prop.getProperty(key).equalsIgnoreCase(name)) {
					String aliasKey = key.substring(0, key.lastIndexOf(".")) + "." + season;
					if (prop.containsKey(aliasKey)) {
						return Integer.parseInt(prop.getProperty(aliasKey));
					} else {
						return season;
					}
				}
			}
			return season;
		}

		public String getImagesPath() {
			return imagesPath;
		}
	}
}
