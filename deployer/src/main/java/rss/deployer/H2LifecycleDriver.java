package rss.deployer;

import rss.environment.Environment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * User: dikmanm
 * Date: 22/02/2015 10:18
 */
public class H2LifecycleDriver implements LifecycleDriver {

    private final String url;
    private final String username;
    private final String password;

    public H2LifecycleDriver() {
        try {
            Properties props = Environment.getInstance().lookup("database.properties");
            String driverClassname = props.getProperty("jdbc.driverClassName");
            url = props.getProperty("jdbc.url");
            username = props.getProperty("jdbc.username");
            password = props.getProperty("jdbc.password");

            Class.forName(driverClassname);
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
