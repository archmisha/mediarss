package rss.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.configuration.dao.SettingsDao;

/**
 * User: Michael Dikman
 * Date: 13/12/12
 * Time: 21:03
 */
@Service
public class SettingsServiceImpl implements SettingsService {

    @Autowired
    private SettingsDao settingsDao;


    @Override
    public String getPersistentSetting(String key) {
        return settingsDao.getSettings(key);
    }

    @Override
    public void setPersistentSetting(String key, String value) {
        settingsDao.setSettings(key, value);
    }
}
