package uk.casey.request.services;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import uk.casey.models.ProductsTableResponseModel;

public class ProductService implements ProductServiceInterface {

    @Override
    public List<ProductsTableResponseModel> retrieveProductsFromDatabase(UUID userId, Properties properties) throws IOException, SQLException {
        System.out.println("Starting to gather data from DB");
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        }

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        String sql = "SELECT id, name, type, provider, value, category, updated_at FROM products WHERE user_id = ?";

        List<ProductsTableResponseModel> products = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ProductsTableResponseModel response = new ProductsTableResponseModel();
                    response.setId(rs.getInt("id"));
                    response.setName(rs.getString("name"));
                    response.setType(rs.getString("type"));
                    response.setProvider(rs.getString("provider"));
                    response.setValue(rs.getBigDecimal("value"));
                    response.setCategory(rs.getString("category"));
                    response.setUpdatedAt(rs.getTimestamp("updated_at"));
                    products.add(response);
                }
            }
        }
        System.out.println("Products found: " + products.size());
        return products;
    }

    @Override
    public boolean updateProductToDatabase(BigDecimal newValue, int id, UUID userId, Properties properties) throws IOException, SQLException{
        System.out.println("Starting to update DB");

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        }

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        String sql = "UPDATE products SET value = ? WHERE id = ? AND user_id = ?";

        try (Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setBigDecimal(1, newValue);
                statement.setInt(2, id);
                statement.setObject(3, userId);

                int rowsUpdated = statement.executeUpdate();
                return rowsUpdated > 0;
            }  
    }

    @Override
    public boolean createProductInDatabase(UUID userId, String name, String type, String provider, String category, BigDecimal value, Timestamp updated_at, Properties properties) throws IOException, SQLException{
        System.out.println("Starting to update DB");

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        }

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        String sql = "INSERT INTO products (user_id, name, type, provider, category, value, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)"; 

        try (Connection connection = DriverManager.getConnection(url, username, password);
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, userId);
            statement.setString(2, name);
            statement.setString(3, type);
            statement.setString(4, provider);
            statement.setString(5, category); // can be null
            statement.setBigDecimal(6, value);
            statement.setTimestamp(7, updated_at);

            int rowsCreated = statement.executeUpdate();
            return rowsCreated > 0;
        }
    }

    @Override
    public boolean removeProductFromDatabase(UUID userId, int id, Properties properties) throws IOException, SQLException {
        System.out.println("Starting to update DB");

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        }

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        String sql = "DELETE FROM products WHERE id = ? AND user_id = ?";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.setObject(2, userId);

            int rowsRemoved = statement.executeUpdate();
            return rowsRemoved > 0;
        }
    }

}
