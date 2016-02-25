package rss.environment;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

/**
 * User: Michael Dikman
 * Date: 11/12/12
 * Time: 22:30
 */
@Service
public class UrlServiceImpl implements UrlService, ServletContextAware {

    public static final String APP_URL = "http://%s:%d/%s";

    private ServletContext servletContext;

    @Override
    public String getApplicationUrl() {
        String rootContext = getRootContext();
        if (rootContext.startsWith("/")) {
            rootContext = rootContext.substring(1);
        }
        if (!StringUtils.isBlank(rootContext) && !rootContext.endsWith("/")) {
            rootContext = rootContext + "/";
        }
        String settingsRootContext = Environment.getInstance().getWebRootContext();
        if (!StringUtils.isBlank(settingsRootContext)) {
            rootContext = settingsRootContext;
        }
        if (rootContext.equals("/")) {
            rootContext = "";
        }
        return String.format(APP_URL, Environment.getInstance().getWebHostName(), Environment.getInstance().getWebPort(), rootContext);
    }

    private String getRootContext() {
        return servletContext.getContextPath();
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
