package rss.services;

import java.util.Date;

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

	String getTorrentDownloadedPath();

	int getTVComPagesToDownload();

	String getTorrentWatchPath();

	String getAlternativeResourcesPath();

	String getShowAlias(String name);

	int getShowSeasonAlias(String name, int season);

	String getPersistentSetting(String key);

	void setPersistentSetting(String key, String value);

	String getWebRootContext();
}
