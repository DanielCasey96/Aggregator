package uk.casey.request.handlers;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import uk.casey.models.ValueModel;
import uk.casey.request.ProductService;

public class UpdateProductHandler implements HttpHandler {

    private final ProductService productService;

    public UpdateProductHandler(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
         ObjectMapper objectMapper = new ObjectMapper();

        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        // Header validation
        String userId = exchange.getRequestHeaders().getFirst("UserId");
        if(!HandlerHelper.validateHeaders(exchange, userId)) {
            return;
        }

        // URL validation
        String path = exchange.getRequestURI().getPath();
        int id = HandlerHelper.validateUrlWithId(path, "update-value", exchange);
        if (id == -1) {
            return;
        }

        // Parse the request body
        ValueModel valueModel = HandlerHelper.parseRequestBody(exchange, objectMapper, ValueModel.class);

        BigDecimal newValue;
        try {
            newValue = valueModel.getValue();
        } catch (IllegalStateException e) {
            exchange.sendResponseHeaders(400, -1);
            System.out.println("Invalid value: " + e.getMessage());
            return;
        }

        try {
            productService.updateProductToDatabase(newValue, id, java.util.UUID.fromString(userId));
            exchange.sendResponseHeaders(204, -1);
            exchange.getResponseBody().flush();
            exchange.getResponseBody().close();
        } catch (SQLException | IOException e) {
            exchange.sendResponseHeaders(500, -1);
            System.out.println("Error updating DataBase " + e.getMessage());
        }
    }

}