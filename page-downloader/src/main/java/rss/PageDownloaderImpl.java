package rss;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.log.LogService;
import rss.util.Utils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: dikmanm
 * Date: 28/12/12 12:11
 */
@Service
public class PageDownloaderImpl implements PageDownloader {

    private static final String LAST_REDIRECT_URL = "LAST_REDIRECT_URL";

    private static CoolDownStatus coolDownStatus = new CoolDownStatus();

    @Autowired
    private LogService log;

    @Override
    public String downloadPageUntilFound(String url, final Pattern pattern) {
        // download 1000 chars first and then advance by 50
        ResponseStreamExtractor<String> streamExtractor = new ResponseStreamExtractor<String>() {
            @Override
            public String extractResponseStream(HttpClient httpClient, HttpResponse httpResponse, HttpClientContext context) throws Exception {
                InputStream is = extractInputStreamFromResponse(httpResponse);
                BufferedInputStream bis = new BufferedInputStream(is);
                byte[] arr = new byte[1000];
                int read = bis.read(arr);
                String str = new String(arr, "UTF-8");
                Matcher matcher = pattern.matcher(str);
                if (matcher.find()) {
                    return str;
                }

                StringBuilder sb = new StringBuilder(str);
                while (read > -1 && !matcher.find()) {
                    read = bis.read(arr, 0, 100);
                    if (read == -1) {
                        break;
                    }
                    sb.append(new String(arr, 0, read, "UTF-8"));
                    matcher.reset(sb);
                }
                return sb.toString();
            }
        };
        return downloadPage(url, Collections.<String, String>emptyMap(), streamExtractor);
    }

    @Override
    public byte[] downloadData(String url) {
        return downloadPage(url, Collections.<String, String>emptyMap(), new ResponseStreamExtractor<byte[]>() {
            @SuppressWarnings("UnnecessaryLocalVariable")
            @Override
            public byte[] extractResponseStream(HttpClient httpClient, HttpResponse httpResponse, HttpClientContext context) throws Exception {
                byte[] res = IOUtils.toByteArray(httpResponse.getEntity().getContent());
                return res;
            }
        });
    }

    public String downloadPage(String url) {
        return downloadPage(url, Collections.<String, String>emptyMap());
    }

    public String downloadPage(String url, Map<String, String> headers) {
        int retry = 3;
        MediaRSSException ex = null;
        while (retry > 0) {
            try {
                return downloadPage(url, headers, new ResponseStreamExtractor<String>() {
                    @Override
                    public String extractResponseStream(HttpClient httpClient, HttpResponse httpResponse, HttpClientContext context) throws Exception {
                        return IOUtils.toString(extractInputStreamFromResponse(httpResponse), "UTF-8");
                    }
                });
            } catch (PageDownloadException e) {
                if (e.getMessage().contains("Truncated chunk")) {
                    ex = e;
                    retry--;
                } else {
                    throw e;
                }
            }
        }
        throw ex;
    }

    public Pair<String, String> downloadPageWithRedirect(String url) {
        return downloadPage(url, Collections.<String, String>emptyMap(), new ResponseStreamExtractor<Pair<String, String>>() {
            @Override
            public Pair<String, String> extractResponseStream(HttpClient httpClient, HttpResponse httpResponse, HttpClientContext context) throws Exception {
                String lastRedirectUrl = (String) context.getAttribute(LAST_REDIRECT_URL);
                return new ImmutablePair<>(IOUtils.toString(extractInputStreamFromResponse(httpResponse), "UTF-8"), lastRedirectUrl);
            }
        });
    }

    private InputStream extractInputStreamFromResponse(HttpResponse httpResponse) throws IOException {
        InputStream is = httpResponse.getEntity().getContent();
//		Header contentEncoding = httpResponse.getEntity().getContentEncoding();
//		if (contentEncoding != null && contentEncoding.toString().contains("gzip")) {
//			is = new GZIPInputStream(is);
//		}
        return is;
    }

    private <T> T downloadPage(String url, Map<String, String> headers, ResponseStreamExtractor<T> streamExtractor) {
        HttpGet httpGet = new HttpGet(url);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpGet.addHeader(entry.getKey(), entry.getValue());
        }
        return sendRequest(httpGet, streamExtractor);
    }

    public String sendPostRequest(String url, String body) {
//		try {
//			List<NameValuePair> parameters = new ArrayList<>();
//			for (Map.Entry<String, String> entry : params.entrySet()) {
//				parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
//			}

        HttpPost httpPost = new HttpPost(url);

//			if (url.contains("?")) {
//                for (String str : url.substring(url.indexOf("?") + 1).split("&")) {
//					String[] arr = str.split("=");
//                    parameters.add(new BasicNameValuePair(arr[0], arr[1]));
//                }
//            }

//            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters);

        StringEntity entity = new StringEntity(body, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        return sendRequest(httpPost, new ResponseStreamExtractor<String>() {
            @Override
            public String extractResponseStream(HttpClient httpClient, HttpResponse httpResponse, HttpClientContext context) throws Exception {
                return IOUtils.toString(extractInputStreamFromResponse(httpResponse), "UTF-8");
//                    return context.getCookieStore().getCookies();
            }
        });
//		} catch (UnsupportedEncodingException e) {
//			throw new RuntimeException(e.getMessage(), e);
//		}
    }

    private <T> T sendRequest(HttpRequestBase httpRequest, ResponseStreamExtractor<T> streamExtractor) {
        long from = System.currentTimeMillis();
        String url = httpRequest.getURI().toString();
        CloseableHttpClient httpClient = null;
        try {
            coolDownStatus.authorizeAccess(url); // blocks until authorized

            // didnt work
//            if ("true".equalsIgnoreCase(System.getProperty("webproxy")) || Environment.getInstance().useWebProxy()) {
//                if (url.contains(TorrentzParserImpl.NAME)) {
//					httpRequest.setURI(new URL("http://anonymouse.org/cgi-bin/anon-www.cgi/" + url).toURI());
//				}
//			}

            // prevent error: hostname in certificate didn't match: <thepiratebay.se> != <ssl2000.cloudflare.com> OR <ssl2000.cloudflare.com> OR <cloudflare.com> OR <*.cloudflare.com>
            HttpsURLConnection.setDefaultHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(SSLContext.getDefault(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            httpClientBuilder.setSSLSocketFactory(sslConnectionSocketFactory);
            HttpClientContext context = HttpClientContext.create();
            httpClientBuilder.setRedirectStrategy(new LaxRedirectStrategy());
            httpClientBuilder.addInterceptorFirst(new HttpResponseInterceptor() {
                @Override
                public void process(HttpResponse response, HttpContext context)
                        throws HttpException, IOException {
                    if (response.containsHeader("Location")) {
                        Header[] locations = response.getHeaders("Location");
                        if (locations.length > 0) {
                            context.setAttribute(LAST_REDIRECT_URL, locations[0].getValue());
                        }
                    }
                }
            });

            // inverted for easy null handling
            if ("true".equalsIgnoreCase(System.getProperty("proxy"))) {
                HttpHost proxy = new HttpHost("rhvwebcachevip.bastion.europe.hp.com", 8080);
                DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
                httpClientBuilder.setRoutePlanner(routePlanner);
            }

            httpClient = httpClientBuilder.build(); //new DefaultHttpClient();
//			httpClient = new DecompressingHttpClient(defaultHttpClient); // ContentEncodingHttpClient

            HttpConnectionParams.setSoTimeout(httpRequest.getParams(), 30 * 1000); // 130 secs
            HttpConnectionParams.setConnectionTimeout(httpRequest.getParams(), 30 * 1000); // 30 secs
            AutoRetryHttpClient retryClient = new AutoRetryHttpClient(httpClient, new DefaultServiceUnavailableRetryStrategy(3, 100));

            HttpResponse httpResponse = retryClient.execute(httpRequest, context);

            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK /*&&
                httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY &&
				httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_PERMANENTLY*/) {
                throw new PageDownloadException("Url " + url + ": " + httpResponse.getStatusLine());
            }

            return streamExtractor.extractResponseStream(httpClient, httpResponse, context);
        } catch (TruncatedChunkException e) {
            throw new PageDownloadException("Truncated chunk for url: " + url + ". " + e.getMessage());
        } catch (Exception e) {
            if (Utils.isRootCauseMessageContains(e, "timed out")) {
                throw new RecoverableConnectionException("Connection timed out for url: " + url);
            } else if (Utils.isRootCauseMessageContains(e, "Bad Gateway")) {
                throw new RecoverableConnectionException("Bad Gateway for url: " + url);
            } else if (Utils.isRootCauseMessageContains(e, "Connection reset")) {
                throw new RecoverableConnectionException("Connection reset for url: " + url);
//			} else if (Utils.getRootCause(e) instanceof java.net.UnknownHostException &&
//					   Utils.isRootCauseMessageContains(e, TVRageServiceImpl.SERVICES_HOSTNAME)) {
//				throw new RecoverableConnectionException("Unknown hostname: " + TVRageServiceImpl.SERVICES_HOSTNAME);
            } else if (Utils.isCauseMessageContains(e, "Invalid redirect URI")) {
                throw new PageDownloadException("Invalid redirect URI: " + url);
            } else if (Utils.isCauseMessageContains(e, "Circular redirect")) {
                throw new PageDownloadException("Circular redirect URI: " + url);
            }
            String errorMessagePrefix = "Failed searching for: " + url + " with error:";
            throw new PageDownloadException(errorMessagePrefix + " " + e.getMessage(), e);
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate de-allocation of all system resources
            if (httpClient != null) {
                try {
                    httpClient.close(); //getConnectionManager().shutdown();
                } catch (IOException e) {
                    log.error(getClass(), e.getMessage(), e);
                }
            }
            log.debug(getClass(), String.format("Download page %s took %d ms", url, System.currentTimeMillis() - from));
        }
    }

    private interface ResponseStreamExtractor<T> {
        public T extractResponseStream(HttpClient httpClient, HttpResponse httpResponse, HttpClientContext context) throws Exception;
    }
}