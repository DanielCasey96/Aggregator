package uk.casey.request.handlers;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import uk.casey.models.ValueModel;
import uk.casey.request.services.ProductServiceInterface;
import uk.casey.utils.JwtUtil;

public class UpdateProductHandler extends HandlerHelper implements HttpHandler {

    private final ProductServiceInterface productServiceInterface;
    private final ObjectMapper objectMapper;

    public UpdateProductHandler(ProductServiceInterface productServiceInterface, ObjectMapper objectMapper) {
        this.productServiceInterface = productServiceInterface;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if(!methodValidation(exchange, "PATCH")) return;

        // Header validation
        Map<String, Predicate<String>> requiredHeaders = new HashMap<>();
        requiredHeaders.put("User-Id", isUUID());
        requiredHeaders.put("Content-Type", isJsonContentType());
        requiredHeaders.put("Authorisation", anyValue());
        HeaderValidationResult headerResult = validateHeaders(exchange, requiredHeaders);
        if (!headerResult.isValid()) return;
        UUID userId = UUID.fromString(headerResult.getValues().get("User-Id"));

        if(!tokenHandling(exchange, headerResult.getValues().get("Authorisation"))) return;

        // URL validation
        int id = validateUrlWithId(exchange.getRequestURI().getPath(), "update", exchange);
        if (id == -1) return;

        // Parse the request body
        ValueModel valueModel = parseRequestBody(exchange, objectMapper, ValueModel.class);

        BigDecimal newValue;
        try {
            newValue = valueModel.getValue();
        } catch (IllegalStateException e) {
            exchange.sendResponseHeaders(400, -1);
            System.out.println("Invalid value: " + e.getMessage());
            return;
        }

        try {
            productServiceInterface.updateProductToDatabase(newValue, id, userId);
            exchange.sendResponseHeaders(204, -1);
            exchange.getResponseBody().flush();
            exchange.getResponseBody().close();
        } catch (SQLException | IOException e) {
            exchange.sendResponseHeaders(500, -1);
            System.out.println("Error updating DataBase " + e.getMessage());
        }
    }

}