package uk.casey.request;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import uk.casey.request.handlers.HandlerHelper;
import uk.casey.request.handlers.NewProductHandler;
import uk.casey.request.services.ProductService;
import uk.casey.request.services.ProductServiceInterface;
import uk.casey.utils.JwtUtil;

public class NewProductHandlerTest {
    private HttpExchange exchange;
    private ProductServiceInterface productServiceInterface;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        exchange = mock(HttpExchange.class);
        productServiceInterface = mock(ProductServiceInterface.class);
        jwtUtil = mock(JwtUtil.class);
    }

    @Tag("unit-integration")
    @Test
    void newProductHandlerSuccess() throws IOException, SQLException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "12341234-1234-1234-1234-123412341234");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");

        String json = """
    {
        "userId": "12341234-1234-1234-1234-123412341234",
        "name": "Holiday Pot",
        "type": "Savings",
        "provider": "Lloyds",
        "category": null,
        "value": 78.24,
        "updatedAt": "2024-06-10T13:34:56.000Z"
    }
    """;

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/add-product"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            String token = headers.getFirst("Authorisation");
            jwtUtilMock.when(() -> JwtUtil.validateToken(token)).thenReturn(true);


            // Stub productService as needed
            when(productServiceInterface.createProductInDatabase(
                    any(UUID.class), anyString(), anyString(), anyString(), anyString(),
                    any(BigDecimal.class), any(Timestamp.class)
            )).thenReturn(true);

            NewProductHandler handler = new NewProductHandler(productServiceInterface, jwtUtil);
            handler.handle(exchange);

            verify(exchange).getRequestMethod();
            verify(exchange, times(3)).getRequestHeaders();
            verify(exchange).sendResponseHeaders(201, -1);
        }
    }

    @Tag("unit-integration")
    @Test
    void newProductIsFlushedAndClosedAfterSuccess() throws IOException, SQLException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "12341234-1234-1234-1234-123412341234");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");

        String json = """
                {
                    "userId": "12341234-1234-1234-1234-123412341234",
                    "name": "Holiday Pot",
                    "type": "Savings",
                    "provider": "Lloyds",
                    "category": null,
                    "value": 78.24,
                    "updatedAt": "2024-06-10T13:34:56.000Z"
                }
                """;

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/add-product"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));
        OutputStream responseBody = mock(OutputStream.class);
        when(exchange.getResponseBody()).thenReturn(responseBody);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            String token = headers.getFirst("Authorisation");
            jwtUtilMock.when(() -> JwtUtil.validateToken(token)).thenReturn(true);

            // Stub productService as needed
            when(productServiceInterface.createProductInDatabase(
                    any(UUID.class), anyString(), anyString(), anyString(), anyString(),
                    any(BigDecimal.class), any(Timestamp.class)
            )).thenReturn(true);

            NewProductHandler handler = new NewProductHandler(productServiceInterface, jwtUtil);
            handler.handle(exchange);

            verify(responseBody).flush();
            verify(responseBody).close();
        }
    }

    @Tag("unit-integration")
    @Test
    void returns405ForNonPostMethod() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("GET");

        NewProductHandler handler = new NewProductHandler(productServiceInterface, jwtUtil);
        handler.handle(exchange);

        verify(exchange).getRequestMethod();
        verify(exchange).sendResponseHeaders(405, -1);
        verifyNoMoreInteractions(exchange);
    }

    @Tag("unit-integration")
    @Test
    void returns400ForMissingContentType() throws IOException {
        Headers headers = new Headers();
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        NewProductHandler handler = new NewProductHandler(productServiceInterface, jwtUtil);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns400ForContentTypeValueIncorrect() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/txt");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        NewProductHandler handler = new NewProductHandler(productServiceInterface, jwtUtil);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns400ForMissingUserId() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        NewProductHandler handler = new NewProductHandler(productServiceInterface, jwtUtil);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns400ForUserIdIncorrectValue() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "notAUUIDFormat");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        NewProductHandler handler = new NewProductHandler(productServiceInterface, jwtUtil);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Test
    void validUrlReturnsSuccess() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        boolean result = HandlerHelper.validateUrlNoId("/add-product", "add-product", exchange);
        assertEquals(true, result);
        verify(exchange, never()).sendResponseHeaders(anyInt(), anyLong());
    }

    @Test
    void invalidUrlReturns404() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        boolean result = HandlerHelper.validateUrlNoId("/add-product/chicken", "add-product", exchange);
        verify(exchange).sendResponseHeaders(404, -1);
        assertEquals(false, result);
    }

    @Tag("unit-integration")
    @Test
    void returns500WhenDatabaseUpdateFailsIOException() throws Exception {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");

        String json = """
    {
        "userId": "123e4567-e89b-12d3-a456-426614174000",
        "name": "ISA",
        "type": "investment",
        "provider": "vanguard",
        "category": null,
        "value": 13567,
        "updatedAt": "2025-09-10T12:00:00"
    }
    """;

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/add-product"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));
        doThrow(new IOException("DB error IO")).when(productServiceInterface).createProductInDatabase(
                any(UUID.class), anyString(), anyString(), anyString(), any(),
                any(BigDecimal.class), any(Timestamp.class)
        );

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            String token = headers.getFirst("Authorisation");
            jwtUtilMock.when(() -> JwtUtil.validateToken(token)).thenReturn(true);

            NewProductHandler handler = new NewProductHandler(productServiceInterface, jwtUtil);
            handler.handle(exchange);

            verify(exchange).sendResponseHeaders(500, -1);
        }
    }

    @Tag("unit-integration")
    @Test
    void returns500WhenDatabaseUpdateFailsSQLException() throws Exception {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");

        String json = """
    {
        "userId": "123e4567-e89b-12d3-a456-426614174000",
        "name": "ISA",
        "type": "investment",
        "provider": "vanguard",
        "category": null,
        "value": 13567,
        "updatedAt": "2025-09-10T12:00:00"
    }
    """;

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/add-product"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));
        doThrow(new SQLException("DB error SQL")).when(productServiceInterface).createProductInDatabase(
                any(UUID.class), anyString(), anyString(), anyString(), any(),
                any(BigDecimal.class), any(Timestamp.class)
        );

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            String token = headers.getFirst("Authorisation");
            jwtUtilMock.when(() -> JwtUtil.validateToken(token)).thenReturn(true);

            NewProductHandler handler = new NewProductHandler(productServiceInterface, jwtUtil);
            handler.handle(exchange);

            verify(exchange).sendResponseHeaders(500, -1);
        }
    }

}
