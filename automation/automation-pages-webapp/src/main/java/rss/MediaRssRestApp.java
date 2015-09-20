package rss;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * ran.biron@hp.com
 * Date: 25/02/13
 */
@ApplicationPath("/rest/*")
public class MediaRssRestApp extends ResourceConfig {

    public MediaRssRestApp() {
        packages("rss");
        register(org.glassfish.jersey.server.spring.SpringComponentProvider.class);
    }
}
