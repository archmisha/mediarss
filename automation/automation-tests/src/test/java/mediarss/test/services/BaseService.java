package mediarss.test.services;

import mediarss.test.Reporter;
import mediarss.test.util.WaitUtil;
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
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
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

    @Autowired
    protected Reporter reporter;

    private static boolean isTomcatUp = false;

    private static ThreadLocal<String> jSessionIds = new ThreadLocal<>();

    public void waitForTomcatStartup() {
        if (isTomcatUp) {
            return;
        }

        reporter.info("Waiting for tomcat to start");
        WaitUtil.waitFor(WaitUtil.TIMEOUT_3_MIN, (int) TimeUnit.SECONDS.toMillis(2), new Runnable() {
            @Override
            public void run() {
                try {
                    String response = sendGetRequest("generate/?user=1&type=shows&feedId=1");
                    reporter.info("...");
                    assertTrue(response.length() > 0);
//                    assertEquals("Failed generating feed. Please contact support for assistance", response);
                    isTomcatUp = true;
                } catch (Exception e) {
                    reporter.info("Waiting for tomcat to start: " + e.getMessage());
                    throw e;
                }
            }
        });
        reporter.info("Tomcat is up");
    }

    protected String sendGetRequest(String url) {
        try {
            HttpGet httpGet = new HttpGet(getServerBaseUrl() + url);
            return sendRequest(httpGet);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected String sendPostRequest(String url, Map<String, Object> params) {
        try {
            HttpPost httpPost = new HttpPost(getServerBaseUrl() + url);
            List<NameValuePair> parameters = new ArrayList<>();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(parameters));
            return sendRequest(httpPost);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String sendRequest(HttpRequestBase httpRequest) throws IOException {
        setJSessionId(httpRequest);

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpClient httpClient = httpClientBuilder.build();

        HttpResponse httpResponse = httpClient.execute(httpRequest);
        if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new RuntimeException("Request failed with status: " + httpResponse.getStatusLine());
        }

        extractJSessionId(httpResponse);
        return IOUtils.toString(httpResponse.getEntity().getContent());
    }

    private void setJSessionId(HttpRequestBase httpRequest) {
        if (jSessionIds.get() != null) {
            httpRequest.setHeader(new BasicHeader("Cookie", "JSESSIONID=" + jSessionIds.get() + ";"));
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
