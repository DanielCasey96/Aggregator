package uk.casey.request.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import uk.casey.models.ProductRequestModel;
import uk.casey.request.services.ProductServiceInterface;
import uk.casey.utils.JwtUtil;

public class NewProductHandler extends HandlerHelper implements HttpHandler {

    private final ProductServiceInterface productServiceInterface;
    private final ObjectMapper objectMapper;

    public NewProductHandler(ProductServiceInterface productServiceInterface, ObjectMapper objectMapper) {
        this.productServiceInterface = productServiceInterface;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        ProductRequestModel prm;

        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        Map<String, Predicate<String>> requiredHeaders = new HashMap<>();
        requiredHeaders.put("User-Id", isUUID());
        requiredHeaders.put("Content-Type", isJsonContentType());
        requiredHeaders.put("Authorisation", anyValue());
        HeaderValidationResult headerResult = validateHeaders(exchange, requiredHeaders);
        if (!headerResult.isValid()) return;
        UUID userId = UUID.fromString(headerResult.getValues().get("User-Id"));

        String token = headerResult.getValues().get("Authorisation");
        if (!JwtUtil.validateToken(token)) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        // URL validation
        String path = exchange.getRequestURI().getPath();
        validateUrlNoId(path, "add-product", exchange);

        // Parse the request body
        prm  = parseRequestBody(exchange, objectMapper, ProductRequestModel.class);

        try {
            productServiceInterface.createProductInDatabase(userId, prm.getName(), prm.getType(), prm.getProvider(), prm.getCategory(), prm.getValue(), prm.getUpdatedAt());
            exchange.sendResponseHeaders(201, -1);
            exchange.getResponseBody().flush();
            exchange.getResponseBody().close();
        } catch (SQLException | IOException | IllegalStateException e) {
            exchange.sendResponseHeaders(500, -1);
            System.err.println("Error updating DataBase " + e.getMessage());
        }
    }
}