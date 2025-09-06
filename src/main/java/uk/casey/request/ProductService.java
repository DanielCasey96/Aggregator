package uk.casey.request;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import uk.casey.models.ProductsTableResponse;

public class ProductService {

    public List<ProductsTableResponse> retrieveProductsFromDatabase(int userId, List<Integer> productIds) throws IOException, SQLException {
        System.out.println("Starting to gather data from DB");
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        }

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < productIds.size(); i++) {
        inClause.append("?");
        if (i < productIds.size() - 1) {
            inClause.append(",");
            }
        }

        String sql = "SELECT id, name, type, provider, value, category, updated_at FROM aggregator WHERE user_id = ? AND id IN (" + inClause + ")";
        // id to be passed from the device
        // user id to be passed from auth

        List<ProductsTableResponse> products = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            for (int i = 0; i < productIds.size(); i++) {
                statement.setInt(i + 2, productIds.get(i)); 
        }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ProductsTableResponse response = new ProductsTableResponse();
                    response.setId(rs.getInt("id"));
                    response.setName(rs.getString("name"));
                    response.setType(rs.getString("type"));
                    response.setProvider(rs.getString("provider"));
                    response.setCategory(rs.getString("category"));
                    response.setValue(rs.getBigDecimal("value"));
                    response.setUpdatedAt(rs.getTimestamp("updated_at"));
                }
            }
        }
        return products;
    }

    public boolean updateProductToDatabase(BigDecimal newValue, int id) throws IOException, SQLException{
        System.out.println("Starting to update DB");

        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        }

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        String sql = "UPDATE products SET value = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setBigDecimal(1, newValue);
                statement.setInt(2, id);

                int rowsUpdated = statement.executeUpdate();
                return rowsUpdated > 0;
            }  
    }

    public void createProductInDataBase() throws IOException {

    }
}
