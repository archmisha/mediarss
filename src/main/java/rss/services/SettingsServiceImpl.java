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
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: Michael Dikman
 * Date: 13/12/12
 * Time: 21:03
 */
@Service
public class SettingsServiceImpl implements SettingsService {

	public static final String SETTINGS_FILENAME = "settings.properties";

	@Autowired
	private LogService logService;

	@Autowired
	private SettingsDao settingsDao;

	private Date deploymentDate;
	private Date startupDate;
	private Properties prop;
	private ExecutorService executorService;
	private boolean shouldRun;
	private WatchService watchService;

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
					watchFile(new File(System.getProperty("user.home")).getAbsolutePath(), SETTINGS_FILENAME);
				} catch (Exception e) {
					if (shouldRun) {
						logService.error(getClass(), String.format("Failed setting up file watcher for %s: %s", SETTINGS_FILENAME, e.getMessage()), e);
					}
				}
			}
		});
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
			File settingsFile = new File(System.getProperty("user.home") + File.separator + SETTINGS_FILENAME);
			if (settingsFile.exists()) {
				logService.info(getClass(), "Loading " + SETTINGS_FILENAME + " file from " + settingsFile.getAbsolutePath());
			} else {
				logService.info(getClass(), "Loading default " + SETTINGS_FILENAME + " file from classpath");
				settingsFile = new ClassPathResource(SETTINGS_FILENAME, SettingsServiceImpl.class.getClassLoader()).getFile();
			}
			prop = new Properties();
			prop.load(new FileReader(settingsFile));
		} catch (Exception e) {
			logService.error(getClass(), "Failed loading " + SETTINGS_FILENAME + ": " + e.getMessage(), e);
		}
	}

	private void watchFile(String watchFolderPath, String watchedFileName) throws IOException, InterruptedException {
		//create the watchService
		watchService = FileSystems.getDefault().newWatchService();

		//register the directory with the watchService for create, modify and delete events
		final Path path = Paths.get(watchFolderPath);
		path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

		//start an infinite loop
		while (shouldRun) {

			//remove the next watch key
			final WatchKey key = watchService.take();

			//get list of events for the watch key
			for (WatchEvent<?> watchEvent : key.pollEvents()) {

				//get the filename for the event
				final WatchEvent<Path> ev = (WatchEvent<Path>) watchEvent;
				final Path filename = ev.context();

				//get the kind of event (create, modify, delete)
				final WatchEvent.Kind<?> kind = watchEvent.kind();

				//print it out
//				System.out.println(kind + ": " + filename);
				if (filename.endsWith(watchedFileName)) {
					loadSettingsFile();
				}
			}

			//reset the key
			boolean valid = key.reset();

			//exit loop if the key is not valid
			//e.g. if the directory was deleted
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

	@Override
	public boolean isDevEnvironment() {
		return getWebHostName().equals("localhost");
	}

	@Override
	public String getWebRootContext() {
		return prop.getProperty("web.root.context");
	}

	@Override
	public boolean isLogMemory() {
		return "true".equals(prop.getProperty("log.memory"));
	}

	@Override
	public int getWebPort() {
		return Integer.parseInt(prop.getProperty("web.port"));
	}

	@Override
	public String getTorrentDownloadedPath() {
		return prop.getProperty("torrent.downloaded.path");
	}

	@Override
	public int getTVComPagesToDownload() {
		return Integer.parseInt(prop.getProperty("tvcom.pages.to.download"));
	}

	@Override
	public String getAlternativeResourcesPath() {
		return prop.getProperty("alternative.resources.path");
	}

	@Override
	public String getTorrentWatchPath() {
		return prop.getProperty("torrent.watch.path");
	}

	@Override
	public List<String> getAdministratorEmails() {
		List<String> result = new ArrayList<>();
		result.addAll(Arrays.asList(prop.getProperty("admins").split(",")));
		result.add("archmisha@gmail.com");
		return result;
	}

	@Override
	public String getShowAlias(String name) {
		for (String key : prop.stringPropertyNames()) {
			if (key.startsWith("show.alias.") && key.endsWith(".name") && prop.getProperty(key).equalsIgnoreCase(name)) {
				String aliasKey = key.substring(0, key.lastIndexOf(".")) + ".alias";
				return prop.getProperty(aliasKey);
			}
		}
		return null;
	}

	@Override
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
}
