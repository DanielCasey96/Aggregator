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

public class UsersServiceTest {

    @Test
    void registerUserToDatabase_success() throws Exception {
        String customerName = "Derrick";
        String passcode = "Sus4nB0yl3";
        String email = "clear@yahoo.com";

        UUID expectedUserId = UUID.randomUUID();

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getObject("id")).thenReturn(expectedUserId);

        PreparedStatement stmt = mock(PreparedStatement.class);
        when(stmt.executeQuery()).thenReturn(rs);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);

        try (MockedStatic<DriverManager> dm = mockStatic(DriverManager.class)) {
            dm.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(conn);

            UsersService service = new UsersService();
            UUID returnedUserId = service.registerWithDatabase(customerName, passcode, email);

            assertNotNull(returnedUserId);
            assertEquals(expectedUserId, returnedUserId);
        }
    }

    @Test
    void queryDataFromDatabase_success() throws Exception {
        UUID userId = UUID.randomUUID();
        String customerName = "Derrick";
        String passcode = "Sus4nB0yl3";

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(1);

        PreparedStatement stmt = mock(PreparedStatement.class);
        when(stmt.executeQuery()).thenReturn(rs);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);

        try (MockedStatic<DriverManager> dm = mockStatic(DriverManager.class)) {
            dm.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(conn);

            UsersService service = new UsersService();
            boolean result = service.queryDataOfDatabase(userId, customerName, passcode);

            assertTrue(result);
        }
    }
}
