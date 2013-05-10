package rss.services;

import java.util.Date;
import java.util.List;

/**
 * User: Michael Dikman
 * Date: 13/12/12
 * Time: 21:02
 */
public interface SettingsService {

	void setDeploymentDate(Date deploymentDate);

	Date getDeploymentDate();

	int getWebPort();

	String getWebHostName();

	boolean isDevEnvironment();

	String getTorrentDownloadedPath();

	int getTVComPagesToDownload();

	String getTorrentWatchPath();

	List<String> getAdministratorEmails();

	String getAlternativeResourcesPath();

	String getShowAlias(String name);

	int getShowSeasonAlias(String name, int season);

	String getPersistentSetting(String key);

	void setPersistentSetting(String key, String value);

	String getWebRootContext();

	boolean isLogMemory();

	void setStartupDate(Date date);

	Date getStartupDate();

	void addUpdateListener(SettingsUpdateListener listener);

	void removeUpdateListener(SettingsUpdateListener listener);

	public interface SettingsUpdateListener {
		void onSettingsUpdated();
	}
}
