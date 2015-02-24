package rss;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * User: dikmanm
 * Date: 28/12/12 12:11
 */
public interface PageDownloader {

    String downloadPageUntilFound(String url, Pattern pattern);

    String downloadPage(String url);

    byte[] downloadData(String url);

    String downloadPage(String url, Map<String, String> headers);

    Pair<String, String> downloadPageWithRedirect(String url);

    String sendPostRequest(String url, String body);
}
