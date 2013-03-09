package rss.dao;

import rss.entities.Setting;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface SettingsDao extends Dao<Setting> {

	String getSettings(String key);

	void setSettings(String key, String value);
}
