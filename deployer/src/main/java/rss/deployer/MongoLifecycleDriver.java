package rss.deployer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rss.rms.driver.MongoDriver;

/**
 * User: dikmanm
 * Date: 21/02/2015 21:30
 */
@Component
public class MongoLifecycleDriver implements LifecycleDriver {

    @Autowired
    private MongoDriver mongoDriver;

    @Override
    public void create() {
        mongoDriver.createDatabase();
    }

    @Override
    public void tearDown() {
        mongoDriver.dropDatabase();
    }
}
