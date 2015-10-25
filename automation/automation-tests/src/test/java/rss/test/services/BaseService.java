package rss.test.services;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rss.test.Reporter;
import rss.test.util.JsonTranslation;
import rss.test.util.WaitUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * User: dikmanm
 * Date: 12/02/2015 22:43
 */
@Component
public class BaseService {

    private static boolean isTomcatUp = false;
    private static boolean isTomcatStartFailed = false;
    private static ThreadLocal<String> jSessionIds = new ThreadLocal<>();
    @Autowired
    protected Reporter reporter;

    public boolean waitForTomcatStartup() {
        if (isTomcatStartFailed) {
            return false;
        }
        if (isTomcatUp) {
            return true;
        }

        reporter.info("Waiting for tomcat to start for 3 mins");
        WaitUtil.waitFor(WaitUtil.TIMEOUT_3_MIN, (int) TimeUnit.SECONDS.toMillis(5), new Runnable() {
            @Override
            public void run() {
                try {
                    String response = sendGetRequest("generate/?user=1&type=shows&feedId=1");
                    reporter.info("...");
                    assertTrue(response.length() > 0);
//                    assertEquals("Failed generating feed. Please contact support for assistance", response);
                    isTomcatUp = true;
                } catch (Exception e) {
                    isTomcatStartFailed = true;
                    reporter.info("Waiting for tomcat to start: " + e.getMessage());
                    throw e;
                }
            }
        });
        reporter.info("Tomcat is up");
        return true;
    }

    protected String sendGetRequest(String url) {
        return sendGetRequest(url, Collections.<String, String>emptyMap());
    }

    protected String sendGetRequest(String url, Map<String, String> queryParams) {
        try {
            URIBuilder uriBuilder = new URIBuilder(getServerBaseUrl() + url);
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                uriBuilder = uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            return sendRequest(httpGet);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected String sendPostRequest(String url, Object params) {
        try {
            HttpPost httpPost = new HttpPost(getServerBaseUrl() + url);
            if (params != null) {
                httpPost.setEntity(new StringEntity(JsonTranslation.object2JsonString(params)));
            }
            httpPost.setHeader("Content-Type", "application/json");
            return sendRequest(httpPost);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected String sendFormPostRequest(String url, Map<String, Object> params) {
        try {
            HttpPost httpPost = new HttpPost(getServerBaseUrl() + url);
            List<NameValuePair> parameters = new ArrayList<>();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(parameters));
            return sendRequest(httpPost);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String sendRequest(HttpRequestBase httpRequest) {
        try {
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            httpClientBuilder.setRedirectStrategy(new LaxRedirectStrategy());
            setJSessionId(httpRequest, httpClientBuilder);
            HttpClient httpClient = httpClientBuilder.build();

            HttpResponse httpResponse = httpClient.execute(httpRequest);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN) {
                throw new NoPermissionsException("Request '" + httpRequest.getURI().toString() + "'");
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException("Request failed with status: " + httpResponse.getStatusLine());
            }

            extractJSessionId(httpResponse);
            String result = IOUtils.toString(httpResponse.getEntity().getContent());

            // if was redirected to root
            if (!httpRequest.getURI().toString().equals("/") &&
                    result.contains("<title>Personalized Media RSS</title>")) {
                throw new RedirectToRootException();
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void setJSessionId(HttpRequestBase httpRequest, HttpClientBuilder httpClientBuilder) {
        if (jSessionIds.get() != null) {
            httpRequest.setHeader(new BasicHeader("Cookie", "JSESSIONID=" + jSessionIds.get() + ";"));
//            CookieStore cookieStore = new BasicCookieStore();
//            BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", jSessionIds.get());
//            cookieStore.addCookie(cookie);
//            cookie.setPath("/");
//            httpClientBuilder.setDefaultCookieStore(cookieStore);
        }
    }

    private void extractJSessionId(HttpResponse httpResponse) {
        Header[] headers = httpResponse.getHeaders("Set-Cookie");
        if (headers == null || headers.length == 0) {
            return;
        }
        String value = headers[0].getValue();
        for (String s : value.split(";")) {
            String[] arr = s.split("=");
            if (arr[0].equals("JSESSIONID")) {
                jSessionIds.set(arr[1]);
                break;
            }
        }

    }

    private String getServerBaseUrl() {
        return "http://" + getServerHost() + ":" + getServerPort() + "/";
    }

    private int getServerPort() {
        return Integer.parseInt(System.getProperty("server.port", "9066"));
    }

    private String getServerHost() {
        return "localhost";
    }

    protected Map<String, Object> entityToMap(Object entity) {
        return new ObjectMapper().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL)
                .convertValue(entity, new TypeReference<Map<String, Object>>() {
                });
    }
}
