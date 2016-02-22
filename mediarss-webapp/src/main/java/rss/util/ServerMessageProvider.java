package rss.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * User: Michael Dikman
 * Date: 11/05/12
 * Time: 13:46
 */
public class ServerMessageProvider {
	private static final String PROPS_NAME = "rss.ServerMessages";
	private static Logger LOG = LogManager.getLogger(ServerMessageProvider.class.getName());

	private static ServerMessageProvider _instance = null;
	private static HashMap<Locale, ResourceBundle> rbMap = new HashMap<Locale, ResourceBundle>();

	//Once this field is set, it cannot be set to null, it can only be changed to a different locale.
	private Locale locale;

	private ServerMessageProvider(Locale localeName) {
		this.locale = localeName;
	}

	public static ServerMessageProvider get() {
		return get(null);
	}

	public static ServerMessageProvider get(Locale locale) {
		if (_instance == null) {
			_instance = new ServerMessageProvider(locale);
		} else {
			//set the locale only in case the new to be set locale is not empty.
			if (locale != null) {
				_instance.setLocale(locale);
			}
		}
		//in all the cases, return the singleton instance.
		return _instance;
	}

	public String getString(String key) {
		return getString(key, locale);
	}

	private void setLocale(Locale locale) {
		this.locale = locale;
	}

	public String getString(String key, Locale locale) {
		if (locale == null) {
			locale = Locale.getDefault();
		}

		return getText(key, locale);
	}

	private String getText(String key, Locale locale) {
		try {
			if (locale == null) {
				locale = Locale.ENGLISH;
			}
			ResourceBundle res = getResourceBundle(locale);

			if (res != null) {
				try {
					return res.getString(key);
				} catch (RuntimeException exc) {
					LOG.warn("Error finding key " + key + " using default value.", exc);
					return key;
				}
			}
		} catch (Exception e) {
			LOG.error("Error getting text from resource bundle for key: " + key, e);
		}
		return key;
	}

	private ResourceBundle getResourceBundle(Locale locale) {
		ResourceBundle rb = rbMap.get(locale);
		if (rb == null) {
			try {
				rb = ResourceBundle.getBundle(PROPS_NAME, locale);
				rbMap.put(locale, rb);
			} catch (MissingResourceException e) {
				LOG.error("The resource bundle was not found for locale: " + locale + ". Moving to english");
				locale = Locale.ENGLISH;
				rb = ResourceBundle.getBundle(PROPS_NAME, locale);
			}
		}
		return rb;
	}
}