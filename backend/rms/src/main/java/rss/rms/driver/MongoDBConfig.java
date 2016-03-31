package rss.rms.driver;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Collections;

/**
 * Created by michaeld on 04/03/2016.
 */
@Configuration
@PropertySource("file:${lookup.dir}/mongodb.properties")
public class MongoDBConfig {

    @Value("${mongodb.host}")
    private String mongodbHost;

    @Value("${mongodb.port}")
    private Integer mongodbPort;

    @Value("${mongodb.username}")
    private String mongodbUsername;

    @Value("${mongodb.password}")
    private String mongodbPassword;

    @Bean(destroyMethod = "close")
    public MongoClient mongo() throws Exception {
        MongoClient mongoClient;
        if (StringUtils.isBlank(mongodbUsername)) {
            mongoClient = new MongoClient(new ServerAddress(mongodbHost, mongodbPort));
        } else {
            MongoCredential credential = MongoCredential.createMongoCRCredential(mongodbUsername, "admin", mongodbPassword.toCharArray());
            mongoClient = new MongoClient(new ServerAddress(mongodbHost, mongodbPort), Collections.singletonList(credential));
        }
        return mongoClient;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}