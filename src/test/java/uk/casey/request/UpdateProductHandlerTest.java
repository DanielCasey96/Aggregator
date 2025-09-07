package uk.casey.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import uk.casey.models.ValueModel;
import uk.casey.request.handlers.UpdateProductHandler;
import java.sql.SQLException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import uk.casey.request.handlers.HandlerHelper;

public class UpdateProductHandlerTest {

    private HttpExchange exchange;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        exchange = mock(HttpExchange.class);
        productService = mock(ProductService.class);
    }

    @Tag("unit-integration")
    @Test
    void updateProductHandlerSuccess() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/update-value/1"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream("{\"value\":123.45}".getBytes()));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));

        UpdateProductHandler handler = new UpdateProductHandler(productService);
        handler.handle(exchange);

        verify(exchange).getRequestMethod();
        verify(exchange, times(2)).getRequestHeaders();
        verify(exchange, times(2)).getResponseBody();
        verify(exchange).sendResponseHeaders(204, -1);
    }

    @Test
    void responseBodyIsFlushedAndClosedAfterSuccessfulUpdate() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/update-value/1"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{\"value\":123.45}".getBytes()));
        OutputStream responseBody = mock(OutputStream.class);
        when(exchange.getResponseBody()).thenReturn(responseBody);

        UpdateProductHandler handler = new UpdateProductHandler(productService);
        handler.handle(exchange);

        verify(responseBody).flush();
        verify(responseBody).close();
    }

    @Tag("unit-integration")
    @Test
    void returns405ForNonPostMethod() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("GET");

        UpdateProductHandler handler = new UpdateProductHandler(productService);
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
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        UpdateProductHandler handler = new UpdateProductHandler(productService);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns400ForContentTypeValueIncorrect() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/txt");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        UpdateProductHandler handler = new UpdateProductHandler(productService);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns400ForMissingUserId() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        UpdateProductHandler handler = new UpdateProductHandler(productService);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns400ForUserIdIncorrectValue() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "notAUUIDFormat");
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        UpdateProductHandler handler = new UpdateProductHandler(productService);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Test
    void validUrlReturnsId() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        int id = HandlerHelper.urlValidation("/update-value/42", "update-value", exchange);
        assertEquals(42, id);
        verify(exchange, never()).sendResponseHeaders(anyInt(), anyLong());
    }

    @Test
    void invalidUrlReturns404() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        int id = HandlerHelper.urlValidation("/update/42", "update-value", exchange);
        assertEquals(-1, id);
        verify(exchange).sendResponseHeaders(404, -1);
    }

    @Test
    void invalidUrlMissingIdReturns404() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        int id = HandlerHelper.urlValidation("/update-value", "update-value", exchange);
        assertEquals(-1, id);
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

    @Test
    void invalidRequestBodyReturns400() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{";

        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(json.getBytes()));

        ValueModel valueModel = HandlerHelper.parseRequestBody(exchange, objectMapper, ValueModel.class);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Test
    void invalidRequestBodyMissingReturns400() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        when(exchange.getRequestBody()).thenReturn(null);

        ValueModel valueModel = HandlerHelper.parseRequestBody(exchange, objectMapper, ValueModel.class);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns500WhenDatabaseUpdateFailsSQLException() throws Exception {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/update-value/1"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{\"value\":123.45}".getBytes()));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));
        doThrow(new SQLException("DB error SQL")).when(productService).updateProductToDatabase(any(BigDecimal.class), anyInt(), any(UUID.class));

        UpdateProductHandler handler = new UpdateProductHandler(productService);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(500, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns500WhenDatabaseUpdateFailsIOException() throws Exception {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/update-value/1"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{\"value\":123.45}".getBytes()));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));
        doThrow(new IOException("DB error IO")).when(productService).updateProductToDatabase(any(BigDecimal.class), anyInt(), any(UUID.class));

        UpdateProductHandler handler = new UpdateProductHandler(productService);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(500, -1);
    }

}
