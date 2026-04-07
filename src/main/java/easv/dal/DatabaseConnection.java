package easv.dal;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {
    private static final String DEFAULT_URL = "jdbc:sqlserver://localhost:1433;databaseName=tickets;encrypt=true;trustServerCertificate=true";
    private static final String CONFIG_FILE = "config.properties";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Properties properties = loadProperties();
        String url = valueOrDefault(properties.getProperty("db.url"), DEFAULT_URL);
        String user = properties.getProperty("db.user");
        String password = valueOrDefault(properties.getProperty("db.password"), "");

        if (user == null || user.isBlank()) {
            return DriverManager.getConnection(url);
        }

        return DriverManager.getConnection(url, user, password);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        try (InputStream inputStream = DatabaseConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not read database config file.", ex);
        }

        return properties;
    }

    private static String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
