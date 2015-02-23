package rss.configuration.dao;

import rss.configuration.entities.Setting;
import rss.ems.dao.Dao;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface SettingsDao extends Dao<Setting> {

	String getSettings(String key);

	void setSettings(String key, String value);
}
