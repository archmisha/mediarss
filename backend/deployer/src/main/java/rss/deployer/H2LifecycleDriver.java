package rss.deployer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * User: dikmanm
 * Date: 22/02/2015 10:18
 */
@Component
public class H2LifecycleDriver implements LifecycleDriver {

    @Value("${jdbc.url}")
    private String url;

    @Value("${jdbc.username}")
    private String username;

    @Value("${jdbc.password}")
    private String password;

    @PostConstruct
    public void init() {
        try {
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
