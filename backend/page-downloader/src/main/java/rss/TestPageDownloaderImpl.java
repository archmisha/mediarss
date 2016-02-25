package rss;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rss.environment.Environment;
import rss.log.LogService;

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

    @Autowired
    private LogService logService;

    private Map<String, String> urlReplacementMap;

    public TestPageDownloaderImpl() {
        urlReplacementMap = new HashMap<>();

        // tvrage
//        urlReplacementMap.put("http://services.tvrage.com/feeds/show_list.php", getLocalUrlPrefix() + "/rest/test-pages/shows/list");
//        urlReplacementMap.put("http://services.tvrage.com/feeds/full_show_info.php", getLocalUrlPrefix() + "/rest/test-pages/shows/info");

        // thetvdb
        urlReplacementMap.put("http://thetvdb.com/api/GetSeries.php\\?seriesname=", getLocalUrlPrefix() + "/rest/test-pages/shows/search?name=");
        urlReplacementMap.put("http://thetvdb.com/api/.*?/series", getLocalUrlPrefix() + "/rest/test-pages/thetvdb/shows/info");
        urlReplacementMap.put("http://thetvdb.com/api/.*?/episodes", getLocalUrlPrefix() + "/rest/test-pages/thetvdb/episodes/info");
        urlReplacementMap.put("http://thetvdb.com/api/Updates.php\\?type=none", getLocalUrlPrefix() + "/rest/test-pages/thetvdb/server-time");
        urlReplacementMap.put("http://thetvdb.com/api/Updates.php\\?type=all&time=", getLocalUrlPrefix() + "/rest/test-pages/thetvdb/updates/");

        // searchers
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
        for (Map.Entry<String, String> entry : urlReplacementMap.entrySet()) {
            url = url.replaceAll(entry.getKey(), entry.getValue());
        }
        return pageDownloader.downloadData(url);
    }

    @Override
    public String downloadPage(String url, Map<String, String> headers) {
        for (Map.Entry<String, String> entry : urlReplacementMap.entrySet()) {
            url = url.replaceAll(entry.getKey(), entry.getValue());
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
