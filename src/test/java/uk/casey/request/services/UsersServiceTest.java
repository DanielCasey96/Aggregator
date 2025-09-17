package uk.casey.request.services;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.casey.models.ProductsTableResponseModel;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import java.util.Properties;

public class UsersServiceTest {

    @Test
    void registerUserToDatabase_success() throws Exception {
        String customerName = "Derrick";
        String passcode = "Sus4nB0yl3";
        String email = "clear@yahoo.com";
        Properties properties = mock(Properties.class);

        UUID expectedUserId = UUID.randomUUID();

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getObject("id")).thenReturn(expectedUserId);

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

            UsersService service = new UsersService(properties);
            UUID returnedUserId = service.registerWithDatabase(customerName, passcode, email);

            assertNotNull(returnedUserId);
            assertEquals(expectedUserId, returnedUserId);
        }
    }

    @Test
    void getHashedPasscodeFromDatabase_success() throws Exception {
        UUID userId = UUID.fromString("3d95aaa8-a189-4f07-b3e0-734c0490b9c3");
        String customerName = "Derrick";
        String expectedHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        Properties properties = mock(Properties.class);

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getString("password")).thenReturn(expectedHash);

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

            UsersService service = new UsersService(properties);
            String result = service.getStoredPassword(userId, customerName);

            assertEquals(expectedHash, result);
            verify(stmt).setObject(1, userId);
            verify(stmt).setString(2, customerName);
        }
    }

    @Test
    void getHashedPasscodeFromDatabase_noValue() throws Exception {
        UUID userId = UUID.randomUUID();
        String customerName = "ghost";
        Properties properties = mock(Properties.class);

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(false);

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

            UsersService service = new UsersService(properties);
            String result = service.getStoredPassword(userId, customerName);

            assertNull(result);
        }
    }
}
