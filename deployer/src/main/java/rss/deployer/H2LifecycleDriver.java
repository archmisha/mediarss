package rss.deployer;

import org.springframework.stereotype.Component;
import rss.environment.Environment;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * User: dikmanm
 * Date: 22/02/2015 10:18
 */
@Component
public class H2LifecycleDriver implements LifecycleDriver {

    private String url;
    private String username;
    private String password;

    @PostConstruct
    public void init() {
        try {
            Properties props = Environment.getInstance().lookup("database.properties");
            url = props.getProperty("jdbc.url");
            username = props.getProperty("jdbc.username");
            password = props.getProperty("jdbc.password");

            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void create() {
    }

    @Override
    public void tearDown() {
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DROP ALL OBJECTS DELETE FILES");
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
