package rss.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;

/**
 * User: Michael Dikman
 * Date: 11/12/12
 * Time: 22:30
 */
@Service
public class UrlServiceImpl implements UrlService {

	public static final String APP_URL = "http://%s:%d/%s";

	@Autowired
	private ServletContext servletContext;

	@Autowired
	private SettingsService settingsService;

	@Override
	public String getApplicationUrl() {
		String rootContext = getRootContext();
		if (rootContext.startsWith("/")) {
			rootContext = rootContext.substring(1);
		}
		if (!StringUtils.isBlank(rootContext) && !rootContext.endsWith("/")) {
			rootContext = rootContext + "/";
		}
		String settingsRootContext = settingsService.getWebRootContext();
		if (!StringUtils.isBlank(settingsRootContext)) {
			rootContext = settingsRootContext;
		}
		if (rootContext.equals("/")) {
			rootContext = "";
		}
		return String.format(APP_URL, settingsService.getWebHostName(), settingsService.getWebPort(), rootContext);
	}

	private String getRootContext() {
		return servletContext.getContextPath();
	}
}
