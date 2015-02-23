package rss.deployer;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import rss.environment.Environment;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;

/**
 * User: dikmanm
 * Date: 21/02/2015 21:30
 */
public class MongoLifecycleDriver implements LifecycleDriver {

    private final MongoClient mongoClient;
    private final String database;

    public MongoLifecycleDriver() {
        try {
            Properties props = Environment.getInstance().lookup("mongodb.properties");

            String host = props.getProperty("mongodb.host");
            Integer port = Integer.valueOf(props.getProperty("mongodb.port"));
            database = props.getProperty("mongodb.database");
            String username = props.getProperty("mongodb.username");
            String password = props.getProperty("mongodb.password");

            MongoCredential credential = MongoCredential.createMongoCRCredential(username, "admin", password.toCharArray());
            mongoClient = new MongoClient(new ServerAddress(host, port), Arrays.asList(credential));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void create() {
        mongoClient.getDB(database);
    }

    @Override
    public void tearDown() {
        mongoClient.dropDatabase(database);
    }
}
