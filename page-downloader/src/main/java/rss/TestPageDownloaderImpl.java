package rss;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rss.environment.Environment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * User: dikmanm
 * Date: 16/08/2015 17:29
 */
@Component("TestPageDownloader")
public class TestPageDownloaderImpl implements PageDownloader {

    @Autowired
    private PageDownloaderImpl pageDownloader;

    private Map<String, String> urlReplacementMap;

    public TestPageDownloaderImpl() {
        urlReplacementMap = new HashMap<>();
        urlReplacementMap.put("http://services.tvrage.com/feeds/show_list.php", getLocalUrlPrefix() + "/rest/test-pages/shows/list");
        urlReplacementMap.put("http://services.tvrage.com/feeds/full_show_info.php", getLocalUrlPrefix() + "/rest/test-pages/shows/info");
        urlReplacementMap.put("http://1337x.net", getLocalUrlPrefix() + "/rest/test-pages/1337x");
    }

    private String getLocalUrlPrefix() {
        return "http://" + Environment.getInstance().getWebHostName() + ":" + Environment.getInstance().getWebPort() + "/test-pages";
    }

    @Override
    public String downloadPageUntilFound(String url, Pattern pattern) {
        return null;
    }

    @Override
    public String downloadPage(String url) {
        return downloadPage(url, Collections.<String, String>emptyMap());
    }

    @Override
    public byte[] downloadData(String url) {
        return new byte[0];
    }

    @Override
    public String downloadPage(String url, Map<String, String> headers) {
        for (Map.Entry<String, String> entry : urlReplacementMap.entrySet()) {
            url = url.replace(entry.getKey(), entry.getValue());
        }
        return pageDownloader.downloadPage(url);
    }

    @Override
    public Pair<String, String> downloadPageWithRedirect(String url) {
        return null;
    }

    @Override
    public String sendPostRequest(String url, String body) {
        return null;
    }
}
