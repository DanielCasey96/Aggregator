package uk.casey.request.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import uk.casey.models.ProductRequestModel;
import uk.casey.request.ProductService;

public class NewProductHandler implements HttpHandler {

    private final ProductService productService;

    public NewProductHandler(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        String userId = exchange.getRequestHeaders().getFirst("UserId");
        if(!HandlerHelper.validateHeaders(exchange, userId)) {
            return;
        }

        // URL validation
        String path = exchange.getRequestURI().getPath();
        HandlerHelper.validateUrlNoId(path, "add-product", exchange);

        // Parse the request body
        ProductRequestModel prm  = HandlerHelper.parseRequestBody(exchange, objectMapper, ProductRequestModel.class);

        try {
            productService.createProductInDataBase("12341234-1234-1234-1234-123412341234", prm.getName(), prm.getType(), prm.getProvider(), prm.getCategory(), prm.getValue(), prm.getUpdatedAt());
            exchange.sendResponseHeaders(201, -1);
            exchange.getResponseBody().flush();
            exchange.getResponseBody().close();
        } catch (SQLException | IOException | IllegalStateException e) {
            exchange.sendResponseHeaders(500, -1);
            System.err.println("Error updating DataBase " + e.getMessage());
        }
    }
}