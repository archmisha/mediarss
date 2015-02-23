package rss.services;

import org.springframework.stereotype.Service;
import rss.environment.UrlService;

/**
 * User: Michael Dikman
 * Date: 11/12/12
 * Time: 22:30
 */
@Service
public class UrlServiceTest implements UrlService {

    public static final String APP_URL = "http://archmisha.no-ip.org:8080%s/"; // %s comes already with a '/' ahead

    @Override
    public String getApplicationUrl() {
        return String.format(APP_URL, getRootContext());
    }

    private String getRootContext() {
        return "/";
    }
}
