package rss.deployer;

import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rss.environment.Environment;

import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * User: dikmanm
 * Date: 21/02/2015 21:30
 */
@Component
public class MongoLifecycleDriver implements LifecycleDriver {

    @Autowired
    private MongoClient mongoClient;

    @Value("${mongodb.database}")
    private String database;

    @Override
    public void create() {
        mongoClient.getDatabase(database);
    }

    @Override
    public void tearDown() {
        mongoClient.dropDatabase(database);
    }
}
