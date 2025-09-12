package uk.casey.request;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.casey.request.handlers.HandlerHelper;
import uk.casey.request.handlers.RemoveProductHandler;
import uk.casey.request.handlers.UpdateProductHandler;
import uk.casey.utils.JwtUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RemoveProductHandlerTest {

    private HttpExchange exchange;
    private ProductService productService;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        exchange = mock(HttpExchange.class);
        productService = mock(ProductService.class);
        jwtUtil = mock(JwtUtil.class);
    }

    @Tag("unit-integration")
    @Test
    void removeProductHandlerSuccess() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");

        when(exchange.getRequestMethod()).thenReturn("DELETE");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/remove-product/1"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            String token = headers.getFirst("Authorisation");
            jwtUtilMock.when(() -> JwtUtil.validateToken(token)).thenReturn(true);

            RemoveProductHandler handler = new RemoveProductHandler(productService, jwtUtil);
            handler.handle(exchange);

            verify(exchange).getRequestMethod();
            verify(exchange, times(3)).getRequestHeaders();
            verify(exchange, times(2)).getResponseBody();
            verify(exchange).sendResponseHeaders(204, -1);
        }
    }

    @Tag("unit-integration")
    @Test
    void removeProductIsFlushedAndClosedAfterSuccessfulUpdate() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");

        when(exchange.getRequestMethod()).thenReturn("DELETE");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/remove-product/1"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));
        OutputStream responseBody = mock(OutputStream.class);
        when(exchange.getResponseBody()).thenReturn(responseBody);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            String token = headers.getFirst("Authorisation");
            jwtUtilMock.when(() -> JwtUtil.validateToken(token)).thenReturn(true);
            RemoveProductHandler handler = new RemoveProductHandler(productService, jwtUtil);
            handler.handle(exchange);

            verify(responseBody).flush();
            verify(responseBody).close();
        }
    }

    @Tag("unit-integration")
    @Test
    void returns405ForNonDeleteMethod() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("POST");

        RemoveProductHandler handler = new RemoveProductHandler(productService, jwtUtil);
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
        when(exchange.getRequestMethod()).thenReturn("DELETE");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        RemoveProductHandler handler = new RemoveProductHandler(productService, jwtUtil);
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
        when(exchange.getRequestMethod()).thenReturn("DELETE");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        RemoveProductHandler handler = new RemoveProductHandler(productService, jwtUtil);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns400ForMissingUserId() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");
        when(exchange.getRequestMethod()).thenReturn("DELETE");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        RemoveProductHandler handler = new RemoveProductHandler(productService, jwtUtil);
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
        when(exchange.getRequestMethod()).thenReturn("DELETE");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        RemoveProductHandler handler = new RemoveProductHandler(productService, jwtUtil);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Test
    void validUrlReturnsId() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        int id = HandlerHelper.validateUrlWithId("/remove-product/42", "remove-product", exchange);
        assertEquals(42, id);
        verify(exchange, never()).sendResponseHeaders(anyInt(), anyLong());
    }

    @Test
    void invalidUrlReturns404() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        int id = HandlerHelper.validateUrlWithId("/remove/chicken/1", "remove-product", exchange);
        assertEquals(-1, id);
        verify(exchange).sendResponseHeaders(404, -1);
    }

    @Test
    void invalidUrlMissingIdReturns404() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        int id = HandlerHelper.validateUrlWithId("/remove-product", "remove-product", exchange);
        verify(exchange).sendResponseHeaders(404, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns500WhenDatabaseUpdateFailsSQLException() throws Exception {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");

        when(exchange.getRequestMethod()).thenReturn("DELETE");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/remove-product/1"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));
        doThrow(new SQLException("DB error SQL")).when(productService).removeProductFromDataBase(any(UUID.class), anyInt());

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            String token = headers.getFirst("Authorisation");
            jwtUtilMock.when(() -> JwtUtil.validateToken(token)).thenReturn(true);

            RemoveProductHandler handler = new RemoveProductHandler(productService, jwtUtil);
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

        when(exchange.getRequestMethod()).thenReturn("DELETE");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/remove-product/1"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));
        doThrow(new IOException("DB error IO")).when(productService).removeProductFromDataBase(any(UUID.class), anyInt());

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            String token = headers.getFirst("Authorisation");
            jwtUtilMock.when(() -> JwtUtil.validateToken(token)).thenReturn(true);

            RemoveProductHandler handler = new RemoveProductHandler(productService, jwtUtil);
            handler.handle(exchange);

            verify(exchange).sendResponseHeaders(500, -1);
        }
    }
}
