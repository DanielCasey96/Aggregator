package uk.casey.request.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.casey.models.ValueModel;

import java.sql.SQLException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import uk.casey.request.services.ProductServiceInterface;
import uk.casey.utils.JwtUtil;

public class UpdateProductHandlerTest {

    private HttpExchange exchange;
    private ProductServiceInterface productServiceInterface;
    private JwtUtil jwtUtil;
    private Properties properties;

    @BeforeEach
    void setUp() {
        exchange = mock(HttpExchange.class);
        productServiceInterface = mock(ProductServiceInterface.class);
        jwtUtil = mock(JwtUtil.class);
        properties = mock(Properties.class);
    }

    @Tag("unit-integration")
    @Test
    void updateProductHandlerSuccess() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/update-value/1"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream("{\"value\":123.45}".getBytes()));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            String token = headers.getFirst("Authorisation");
            jwtUtilMock.when(() -> JwtUtil.validateToken(token)).thenReturn(true);
            UpdateProductHandler handler = new UpdateProductHandler(productServiceInterface, properties);
            handler.handle(exchange);

            verify(exchange).getRequestMethod();
            verify(exchange, times(3)).getRequestHeaders();
            verify(exchange, times(2)).getResponseBody();
            verify(exchange).sendResponseHeaders(204, -1);
        }
    }

    @Test
    void updateProductIsFlushedAndClosedAfterSuccessfulUpdate() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/update-value/1"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{\"value\":123.45}".getBytes()));
        OutputStream responseBody = mock(OutputStream.class);
        when(exchange.getResponseBody()).thenReturn(responseBody);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            String token = headers.getFirst("Authorisation");
            jwtUtilMock.when(() -> JwtUtil.validateToken(token)).thenReturn(true);
            UpdateProductHandler handler = new UpdateProductHandler(productServiceInterface, properties);
            handler.handle(exchange);

            verify(responseBody).flush();
            verify(responseBody).close();
        }
    }

    @Tag("unit-integration")
    @Test
    void returns405ForNonPostMethod() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("GET");

        UpdateProductHandler handler = new UpdateProductHandler(productServiceInterface, properties);
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

        UpdateProductHandler handler = new UpdateProductHandler(productServiceInterface, properties);
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

        UpdateProductHandler handler = new UpdateProductHandler(productServiceInterface, properties);
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

        UpdateProductHandler handler = new UpdateProductHandler(productServiceInterface, properties);
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

        UpdateProductHandler handler = new UpdateProductHandler(productServiceInterface, properties);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Test
    void validUrlReturnsId() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        int id = HandlerHelper.validateUrlWithId("/update-value/42", "update-value", exchange);
        assertEquals(42, id);
        verify(exchange, never()).sendResponseHeaders(anyInt(), anyLong());
    }

    @Test
    void invalidUrlReturns404() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        int id = HandlerHelper.validateUrlWithId("/update/42", "update-value", exchange);
        assertEquals(-1, id);
        verify(exchange).sendResponseHeaders(404, -1);
    }

    @Test
    void invalidUrlMissingIdReturns404() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        int id = HandlerHelper.validateUrlWithId("/update-value", "update-value", exchange);
        verify(exchange).sendResponseHeaders(404, -1);
    }

    @Test
    void validRequestBodyReturnsValue() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"value\":123.45}";

        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(json.getBytes()));

        ValueModel valueModel = HandlerHelper.parseRequestBody(exchange, objectMapper, ValueModel.class);

        Assertions.assertNotNull(valueModel);
        assertEquals(new BigDecimal("123.45"), valueModel.getValue());
    }

    //Remove this is not specific to the handler
    @Test
    void invalidRequestBodyReturns400() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{";

        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(json.getBytes()));

        HandlerHelper.parseRequestBody(exchange, objectMapper, ValueModel.class);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    //Remove this is not specific to the handler
    @Test
    void invalidRequestBodyMissingReturns400() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        when(exchange.getRequestBody()).thenReturn(null);

        HandlerHelper.parseRequestBody(exchange, objectMapper, ValueModel.class);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns500WhenDatabaseUpdateFailsSQLException() throws Exception {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        headers.add("Authorisation", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MTEyMGFiZi0zMzlkLTQ2MjctODE4OC0xZTI0ZTc3NTk0NzUiLCJ1c2VybmFtZSI6ImNhc2V5MmJvb2dhbG9vIiwiaWF0IjoxNzU3NzA5NzQ5LCJleHAiOjE3NTc3MDk4Njl9.03sPM5GMx0y0SI0H133ng4EhPdCqjDgv6loU-Q-zVqU");

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/update-value/1"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{\"value\":123.45}".getBytes()));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));
        doThrow(new SQLException("DB error SQL")).when(productServiceInterface).updateProductToDatabase(any(BigDecimal.class), anyInt(), any(UUID.class), any(Properties.class));

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            String token = headers.getFirst("Authorisation");
            jwtUtilMock.when(() -> JwtUtil.validateToken(token)).thenReturn(true);
            UpdateProductHandler handler = new UpdateProductHandler(productServiceInterface, properties);
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

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/update-value/1"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{\"value\":123.45}".getBytes()));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));
        doThrow(new IOException("DB error IO")).when(productServiceInterface).updateProductToDatabase(any(BigDecimal.class), anyInt(), any(UUID.class), any(Properties.class));

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            String token = headers.getFirst("Authorisation");
            jwtUtilMock.when(() -> JwtUtil.validateToken(token)).thenReturn(true);
            UpdateProductHandler handler = new UpdateProductHandler(productServiceInterface, properties);
            handler.handle(exchange);

            verify(exchange).sendResponseHeaders(500, -1);
        }
    }

}
