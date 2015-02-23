package rss.configuration;

/**
 * User: Michael Dikman
 * Date: 13/12/12
 * Time: 21:02
 */
public interface SettingsService {

    String getPersistentSetting(String key);

    void setPersistentSetting(String key, String value);
}
