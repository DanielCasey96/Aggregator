package uk.casey.request;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.casey.models.ProductRequestModel;
import uk.casey.request.handlers.HandlerHelper;
import uk.casey.request.handlers.NewProductHandler;
import uk.casey.request.handlers.UpdateProductHandler;
import java.sql.SQLException;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class NewProductHandlerTest {
    private HttpExchange exchange;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        exchange = mock(HttpExchange.class);
        productService = mock(ProductService.class);
    }

    @Tag("unit-integration")
    @Test
    void newProductHandlerSuccess() throws IOException, SQLException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "12341234-1234-1234-1234-123412341234");

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

        // Stub productService as needed
        when(productService.createProductInDataBase(
                anyString(), anyString(), anyString(), anyString(), anyString(),
                any(BigDecimal.class), any(Timestamp.class)
        )).thenReturn(true);

        NewProductHandler handler = new NewProductHandler(productService);
        handler.handle(exchange);

        verify(exchange).getRequestMethod();
        verify(exchange, times(2)).getRequestHeaders();
        verify(exchange).sendResponseHeaders(201, -1);
    }

}
