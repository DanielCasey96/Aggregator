package uk.casey.request.services;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.UUID;

public class UsersService implements UsersServiceInterface {

    public UUID registerWithDatabase(String customerUsername, String passcode, String email, Properties properties) throws IOException, SQLException {
        System.out.println("Starting to update DB");

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        }

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?) RETURNING id";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customerUsername);
            statement.setString(2, passcode);
            statement.setString(3, email);


            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return (UUID) rs.getObject("id");
                } else {
                    throw new SQLException("User ID not returned from database.");
                }
            }
        }
    }

    public String getStoredPassword(UUID userId, String customerUsername, Properties properties) throws IOException, SQLException {
        System.out.println("Starting to update DB");

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        }

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        String sql = "SELECT password FROM users WHERE id = ? AND username = ?";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, userId);
            statement.setString(2, customerUsername);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getString("password");
            }
            return null;
        }
    }

}
