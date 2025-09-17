package uk.casey.request.services;

import org.junit.jupiter.api.Test;
import uk.casey.models.ProductsTableResponseModel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mockStatic;

import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Properties;
import java.util.UUID;
import java.util.List;

public class ProductServiceTest {

    @Test
    void retrieveProductsFromDatabase_returnsProducts() throws Exception {
        UUID userId = UUID.randomUUID();
        Properties properties = mock(Properties.class);

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("name")).thenReturn("Product1");
        when(rs.getString("type")).thenReturn("TypeA");
        when(rs.getString("provider")).thenReturn("ProviderX");
        when(rs.getBigDecimal("value")).thenReturn(BigDecimal.valueOf(100));
        when(rs.getString("category")).thenReturn("Cat1");
        when(rs.getTimestamp("updated_at")).thenReturn(new Timestamp(System.currentTimeMillis()));

        PreparedStatement stmt = mock(PreparedStatement.class);
        when(stmt.executeQuery()).thenReturn(rs);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);

        when(properties.getProperty("db.url")).thenReturn("jdbc:h2:mem:test");
        when(properties.getProperty("db.username")).thenReturn("user");
        when(properties.getProperty("db.password")).thenReturn("pass");

        try (MockedStatic<DriverManager> dm = mockStatic(DriverManager.class)) {
            dm.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
            .thenReturn(conn);

            ProductService service = new ProductService();
            List<ProductsTableResponseModel> products = service.retrieveProductsFromDatabase(userId, properties);

            assertNotNull(products);
            assertEquals(1, products.size());
            assertEquals("Product1", products.get(0).getName());
        }
    }

    @Test
    void updateProductToDatabase_Success() throws Exception {
        BigDecimal value = BigDecimal.valueOf(12.56);
        int id = 1;
        UUID userId = UUID.randomUUID();
        Properties properties = mock(Properties.class);

        PreparedStatement stmt = mock(PreparedStatement.class);
        when(stmt.executeUpdate()).thenReturn(1);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);

        when(properties.getProperty("db.url")).thenReturn("jdbc:h2:mem:test");
        when(properties.getProperty("db.username")).thenReturn("user");
        when(properties.getProperty("db.password")).thenReturn("pass");

        try (MockedStatic<DriverManager> dm = mockStatic(DriverManager.class)) {
            dm.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(conn);

            ProductService service = new ProductService();
            boolean result = service.updateProductToDatabase(value, id, userId, properties);

            assertTrue(result);
        }
    }

    @Test
    void createProductToDatabase_Success() throws Exception {
        UUID userId = UUID.randomUUID();
        String name = "Fredward";
        String type = "bank";
        String provider = "Monzo";
        String category = "savings";
        BigDecimal value = BigDecimal.valueOf(12.67);
        Timestamp updated_at = new Timestamp(System.currentTimeMillis());
        Properties properties = mock(Properties.class);

        PreparedStatement stmt = mock(PreparedStatement.class);
        when(stmt.executeUpdate()).thenReturn(1);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);

        when(properties.getProperty("db.url")).thenReturn("jdbc:h2:mem:test");
        when(properties.getProperty("db.username")).thenReturn("user");
        when(properties.getProperty("db.password")).thenReturn("pass");

        try (MockedStatic<DriverManager> dm = mockStatic(DriverManager.class)) {
            dm.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(conn);

            ProductService service = new ProductService();
            boolean result = service.createProductInDatabase(userId, name, type, provider, category, value, updated_at, properties);

            assertTrue(result);
        }
    }

    @Test
    void removeProductFromDatabase_Success() throws Exception {
        int id = 2;
        UUID userId = UUID.randomUUID();
        Properties properties = mock(Properties.class);

        PreparedStatement stmt = mock(PreparedStatement.class);
        when(stmt.executeUpdate()).thenReturn(1);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);

        when(properties.getProperty("db.url")).thenReturn("jdbc:h2:mem:test");
        when(properties.getProperty("db.username")).thenReturn("user");
        when(properties.getProperty("db.password")).thenReturn("pass");

        try (MockedStatic<DriverManager> dm = mockStatic(DriverManager.class)) {
            dm.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(conn);

            ProductService service = new ProductService();
            boolean result = service.removeProductFromDatabase(userId, id, properties);

            assertTrue(result);
        }
    }

}
