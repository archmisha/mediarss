package rss.services;

import org.apache.http.cookie.Cookie;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * User: dikmanm
 * Date: 28/12/12 12:11
 */
public interface PageDownloader {

	String downloadPageUntilFound(String url, Pattern pattern);

	String downloadPage(String url);

	byte[] downloadImage(String url);

	String downloadPage(String url, Map<String, String> headers);

	List<Cookie> sendPostRequest(String url, Map<String, String> params);
}
