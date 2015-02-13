package mediarss.test.services;

import mediarss.test.Reporter;
import mediarss.test.util.WaitUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertTrue;

/**
 * User: dikmanm
 * Date: 12/02/2015 22:43
 */
@Component
public class BaseService {

    @Autowired
    protected Reporter reporter;

    private static boolean isTomcatUp = false;

    public void waitForTomcatStartup() {
        if (isTomcatUp) {
            return;
        }

        reporter.info("Waiting for tomcat to start");
        WaitUtil.waitFor(WaitUtil.TIMEOUT_3_MIN, (int)TimeUnit.SECONDS.toMillis(2), new Runnable() {
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
            HttpGet httpGet = new HttpGet("http://" + "localhost" + ":" + "9066" + "/" + url);

            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            HttpClient httpClient = httpClientBuilder.build();
            HttpResponse httpResponse = httpClient.execute(httpGet);
            return IOUtils.toString(httpResponse.getEntity().getContent());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
