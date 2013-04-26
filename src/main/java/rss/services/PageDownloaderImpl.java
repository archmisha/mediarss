package rss.services;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.AutoRetryHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.ConnectionTimeoutException;
import rss.MediaRSSException;
import rss.services.log.LogService;
import rss.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * User: dikmanm
 * Date: 28/12/12 12:11
 */
@Service
public class PageDownloaderImpl implements PageDownloader {

	private static CoolDownStatus coolDownStatus = new CoolDownStatus();

	@Autowired
	private LogService log;

	@Override
	public String downloadPageUntilFound(String url, final Pattern pattern) {
		// download 1000 chars first and then advance by 50
		ResponseStreamExtractor<String> streamExtractor = new ResponseStreamExtractor<String>() {
			@Override
			public String extractResponseStream(AbstractHttpClient httpClient, HttpResponse httpResponse) throws Exception {
				InputStream is = extractInputStreamFromResponse(httpResponse);
				byte[] arr = new byte[1000];
				int read = is.read(arr);
				String str = new String(arr);
				Matcher matcher = pattern.matcher(str);
				if (matcher.find()) {
					return str;
				}

				StringBuilder sb = new StringBuilder(str);
				while (read > -1 && !matcher.find()) {
					read = is.read(arr, 0, 50);
					if (read == -1) {
						break;
					}
					sb.append(new String(arr, 0, read));
					matcher = pattern.matcher(sb);
				}
				return sb.toString();
			}
		};
		return downloadPage(url, Collections.<String, String>emptyMap(), streamExtractor);
	}

	@Override
	public byte[] downloadImage(String url) {
		return downloadPage(url, Collections.<String, String>emptyMap(), new ResponseStreamExtractor<byte[]>() {
			@Override
			public byte[] extractResponseStream(AbstractHttpClient httpClient, HttpResponse httpResponse) throws Exception {
				byte[] res = IOUtils.toByteArray(httpResponse.getEntity().getContent());
				return res;
			}
		});
	}

	public String downloadPage(String url) {
		return downloadPage(url, Collections.<String, String>emptyMap());
	}

	public String downloadPage(String url, Map<String, String> headers) {
		return downloadPage(url, headers, new ResponseStreamExtractor<String>() {
			@Override
			public String extractResponseStream(AbstractHttpClient httpClient, HttpResponse httpResponse) throws Exception {
				return IOUtils.toString(extractInputStreamFromResponse(httpResponse));
			}
		});
	}

	private InputStream extractInputStreamFromResponse(HttpResponse httpResponse) throws IOException {
		InputStream is = httpResponse.getEntity().getContent();
		Header contentEncoding = httpResponse.getEntity().getContentEncoding();
		if (contentEncoding != null && contentEncoding.toString().contains("gzip")) {
			is = new GZIPInputStream(is);
		}
		return is;
	}

	private <T> T downloadPage(String url, Map<String, String> headers, ResponseStreamExtractor<T> streamExtractor) {
		long from = System.currentTimeMillis();

		String errorMessagePrefix = "Failed searching for: " + url + " with error:";
		try {
			HttpGet httpGet = new HttpGet(url);

			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpGet.addHeader(entry.getKey(), entry.getValue());
			}

			return sendRequest(httpGet, streamExtractor);
		} catch (Exception e) {
			throw new MediaRSSException(errorMessagePrefix + " " + e.getMessage(), e);
//			log.error(getClass(), errorMessagePrefix + " " + e.getMessage(), e);
//			return null;
		} finally {
			log.debug(getClass(), String.format("Download page %s took %d millis", url, System.currentTimeMillis() - from));
		}
	}

	public List<Cookie> sendPostRequest(String url, Map<String, String> params) {
		try {
			List<NameValuePair> parameters = new ArrayList<>();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}

			HttpPost httpPost = new HttpPost(url);

			if (url.contains("?")) {
				for (String str : url.substring(url.indexOf("?") + 1).split("&")) {
					String[] arr = str.split("=");
					httpPost.getParams().setParameter(arr[0], arr[1]);
				}
			}

			HttpEntity httpEntity = new UrlEncodedFormEntity(parameters);
			httpPost.setEntity(httpEntity);
			return sendRequest(httpPost, new ResponseStreamExtractor<List<Cookie>>() {
				@Override
				public List<Cookie> extractResponseStream(AbstractHttpClient httpClient, HttpResponse httpResponse) throws Exception {
					return httpClient.getCookieStore().getCookies();
				}
			});
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private <T> T sendRequest(HttpRequestBase httpRequest, ResponseStreamExtractor<T> streamExtractor) {
		AbstractHttpClient httpClient = null;
		String url = httpRequest.getURI().toString();
		try {
			coolDownStatus.authorizeAccess(url); // blocks until authorized

			httpClient = new DefaultHttpClient();

			// inverted for easy null handling
			if ("true".equalsIgnoreCase(System.getProperty("proxy"))) {
				HttpHost proxy = new HttpHost("rhvwebcachevip.bastion.europe.hp.com", 8080);
				httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			}

			HttpConnectionParams.setSoTimeout(httpRequest.getParams(), 30 * 1000); // 10 secs
			HttpConnectionParams.setConnectionTimeout(httpRequest.getParams(), 30 * 1000); // 10 secs
			AutoRetryHttpClient retryClient = new AutoRetryHttpClient(httpClient, new DefaultServiceUnavailableRetryStrategy(3, 100));

			HttpResponse httpResponse = null;
			int retries = 3;
			Exception ex = null;
			while (retries > 0) {
				try {
					httpResponse = retryClient.execute(httpRequest);
					retries = -1; // stop retry
				} catch (TruncatedChunkException e) {
					// need to retry
					log.info(getClass(), "Retrying to download page: " + url);
					retries--;
					ex = e;
				}
			}

			if (httpResponse == null) {
				// in that case ex should not be null
				throw new RuntimeException(ex.getMessage(), ex);
			}
			if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK &&
				httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
				throw new RuntimeException("Url " + url + ": " + httpResponse.getStatusLine());
			}

			return streamExtractor.extractResponseStream(httpClient, httpResponse);
		} catch (Exception e) {
			if (Utils.isRootCauseMessageContains(e, "Connection timed out")) {
				throw new ConnectionTimeoutException("Url: " + url + ". Connection timed out", e);
			}
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate de-allocation of all system resources
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
	}

	private interface ResponseStreamExtractor<T> {
		public T extractResponseStream(AbstractHttpClient httpClient, HttpResponse httpResponse) throws Exception;
	}
}
