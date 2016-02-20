package rss.environment;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * User: dikmanm
 * Date: 21/02/2015 13:47
 */
public class Environment {

    public static final String SETTINGS_FILENAME = "settings.properties";
    public static final String ADMIN_DEFAULT_EMAIL = "archmisha@gmail.com";
    private static final Logger LOGGER = LoggerFactory.getLogger(Environment.class);
    private static Environment instance;

    private Date deploymentDate;
    private Date startupDate;
    private ExecutorService executorService;
    private boolean shouldRun;
    private WatchService watchService;

    private SettingsBean settingsBean;

    private Collection<SettingsUpdateListener> updateListeners = new ArrayList<>();
    private String lookupDir;

    private Environment() {
        shouldRun = true;
        lookupDir = System.getProperty("lookup.dir");
        LOGGER.info("Lookup dir set to '{}'", lookupDir);
        loadSettingsFile();

        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    watchFile(new File(System.getProperty("lookup.dir")).getAbsolutePath(), SETTINGS_FILENAME);
                } catch (Exception e) {
                    if (shouldRun) {
                        LOGGER.error(String.format("Failed setting up file watcher for %s: %s", SETTINGS_FILENAME, e.getMessage()), e);
                    }
                }
            }
        });
        LOGGER.info("Environment init complete");
    }

    public static Environment getInstance() {
        if (instance == null) {
            instance = new Environment();
        }
        return instance;
    }

    /* package for tests */
    static void setInstance(Environment environment) {
        instance = environment;
    }

    public void shutdown() {
        LOGGER.info("Terminating settings service and watch job");
        shouldRun = false;
        executorService.shutdown();
        try {
            if (watchService != null) {
                watchService.close();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void loadSettingsFile() {
        LOGGER.info("Loading " + SETTINGS_FILENAME + " file");
        try {
            Properties prop = lookup(SETTINGS_FILENAME);
            settingsBean = new SettingsBean(prop);
        } catch (Exception e) {
            LOGGER.error("Failed loading " + SETTINGS_FILENAME + ": " + e.getMessage(), e);
        }

        // call all registered listeners
        for (SettingsUpdateListener updateListener : updateListeners) {
            try {
                updateListener.onSettingsUpdated();
            } catch (Exception e) {
                LOGGER.error("Failed updating settings listener: " + e.getMessage(), e);
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

    public Date getDeploymentDate() {
        return deploymentDate;
    }

    public void setDeploymentDate(Date deploymentDate) {
        this.deploymentDate = deploymentDate;
    }

    public Date getStartupDate() {
        return startupDate;
    }

    public void setStartupDate(Date startupDate) {
        this.startupDate = startupDate;
    }

    public boolean useWebProxy() {
        return settingsBean.useWebProxy();
    }

    public String getWebHostName() {
        return settingsBean.getWebHostName();
    }

    public String getWebRootContext() {
        return settingsBean.getWebRootContext();
    }

    public String getTrackerUrl() {
        return settingsBean.getTrackerUrl();
    }

    public String getImagesPath() {
        return settingsBean.getImagesPath();
    }

    public boolean isLogMemory() {
        return settingsBean.isLogMemory();
    }

    public ServerMode getServerMode() {
        // if there is a system property use it, fallback to settings
        String value = System.getProperty("server.mode");
        try {
            if (value == null) {
                return settingsBean.getServerMode();
            } else {
                return ServerMode.valueOf(value);
            }
        } catch (IllegalArgumentException e) {
            return settingsBean.getServerMode();
        }
    }

    public String getServerHostUrl() {
        return "http://" + getWebHostName() + ":" + getWebPort();
    }

    public boolean areSubtitlesEnabled() {
        return settingsBean.areSubtitlesEnabled();
    }

    public int getWebPort() {
        return settingsBean.getWebPort();
    }

    public String getTorrentDownloadedPath() {
        return settingsBean.getTorrentDownloadedPath();
    }

    public int getTVComPagesToDownload() {
        return settingsBean.getTVComPagesToDownload();
    }

    public String getAlternativeResourcesPath() {
        return settingsBean.getAlternativeResourcesPath();
    }

    public String getTorrentWatchPath() {
        return settingsBean.getTorrentWatchPath();
    }

    public Set<String> getAdministratorEmails() {
        return settingsBean.getAdminEmails();
    }

    public String getShowAlias(String name) {
        return settingsBean.getShowAlias(name);
    }

    public int getShowSeasonAlias(String name, int season) {
        return settingsBean.getShowSeasonAlias(name, season);
    }

    public void addUpdateListener(SettingsUpdateListener listener) {
        this.updateListeners.add(listener);
    }

    public void removeUpdateListener(SettingsUpdateListener listener) {
        this.updateListeners.remove(listener);
    }

    public Properties lookup(String filename) {
        try {
            Properties props = new Properties();
            props.load(new FileReader(lookupDir + File.separator + filename));
            return props;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private class SettingsBean {
        private Properties prop;

        private Set<String> adminEmails;
        private boolean logMemory;
        private int tvComPagesToDownload;
        private int webPort;
        private boolean areSubtitlesEnabled;
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
                if (StringUtils.isBlank(value) || value.equals("localhost")) {
                    value = InetAddress.getLocalHost().getCanonicalHostName();
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

        public ServerMode getServerMode() {
            String value = prop.getProperty("server.mode");
            if (StringUtils.isBlank(value)) {
                value = "dev";
            }
            return ServerMode.valueOf(value.toUpperCase());
        }
    }
}
