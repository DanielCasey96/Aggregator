package uk.casey.request;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.casey.request.handlers.HandlerHelper;
import uk.casey.request.handlers.RetrievalHandler;
import uk.casey.request.services.ProductServiceInterface;
import uk.casey.utils.JwtUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RetrievalHandlerTest {

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
    void retrieveHandlerSuccess() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");

        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/accounts"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            String token = headers.getFirst("Authorisation");
            jwtUtilMock.when(() -> JwtUtil.validateToken(token)).thenReturn(true);
            RetrievalHandler handler = new RetrievalHandler(productServiceInterface, jwtUtil);
            handler.handle(exchange);

            verify(exchange).getRequestMethod();
            verify(exchange, times(3)).getRequestHeaders();
            verify(exchange).sendResponseHeaders(200, 2L);
            verify(exchange, times(3)).getResponseBody();
        }
    }

    @Test
    void responseBodyIsFlushedAndClosedAfterSuccessfulRetrieval() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");

        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/accounts"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        OutputStream responseBody = mock(OutputStream.class);
        when(exchange.getResponseBody()).thenReturn(responseBody);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            String token = headers.getFirst("Authorisation");
            jwtUtilMock.when(() -> JwtUtil.validateToken(token)).thenReturn(true);
            RetrievalHandler handler = new RetrievalHandler(productServiceInterface, jwtUtil);
            handler.handle(exchange);

            verify(responseBody).flush();
            verify(responseBody).close();
        }
    }

    @Tag("unit-integration")
    @Test
    void returns405ForNonGetMethod() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("POST");

        RetrievalHandler handler = new RetrievalHandler(productServiceInterface, jwtUtil);
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
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        RetrievalHandler handler = new RetrievalHandler(productServiceInterface, jwtUtil);
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
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        RetrievalHandler handler = new RetrievalHandler(productServiceInterface, jwtUtil);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns400ForMissingUserId() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        RetrievalHandler handler = new RetrievalHandler(productServiceInterface, jwtUtil);
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
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        RetrievalHandler handler = new RetrievalHandler(productServiceInterface, jwtUtil);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Test
    void validUrlReturnsSuccess() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        boolean result = HandlerHelper.validateUrlNoId("/accounts", "accounts", exchange);
        assertEquals(true, result);
        verify(exchange, never()).sendResponseHeaders(anyInt(), anyLong());
    }

    @Test
    void invalidUrlReturns404() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        boolean result = HandlerHelper.validateUrlNoId("/update/42/145", "update-value", exchange);
        verify(exchange).sendResponseHeaders(404, -1);
        assertEquals(false, result);
    }

    @Test
    void returns500WhenDatabaseUpdateFailsSQLException() throws Exception {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");

        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/accounts"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));
        doThrow(new SQLException("DB error SQL")).when(productServiceInterface).retrieveProductsFromDatabase(any(UUID.class));
        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            String token = headers.getFirst("Authorisation");
            jwtUtilMock.when(() -> JwtUtil.validateToken(token)).thenReturn(true);
            RetrievalHandler handler = new RetrievalHandler(productServiceInterface, jwtUtil);
            handler.handle(exchange);

            verify(exchange).sendResponseHeaders(500, -1);
        }
    }

    @Tag("unit-integration")
    @Test
    void returns500WhenDatabaseUpdateFailsIOException() throws Exception {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");

        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/accounts"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));
        doThrow(new IOException("DB error IO")).when(productServiceInterface).retrieveProductsFromDatabase(any(UUID.class));

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            String token = headers.getFirst("Authorisation");
            jwtUtilMock.when(() -> JwtUtil.validateToken(token)).thenReturn(true);

            RetrievalHandler handler = new RetrievalHandler(productServiceInterface, jwtUtil);
            handler.handle(exchange);

            verify(exchange).sendResponseHeaders(500, -1);
        }
    }

}
