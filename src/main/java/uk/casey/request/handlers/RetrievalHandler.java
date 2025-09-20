package uk.casey.request.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import uk.casey.models.ProductsTableResponseModel;
import uk.casey.request.services.ProductServiceInterface;
import uk.casey.utils.JwtUtil;

public class RetrievalHandler implements HttpHandler {

    private final ProductServiceInterface productServiceInterface;
    private final ObjectMapper objectMapper;

    public RetrievalHandler(ProductServiceInterface productServiceInterface, ObjectMapper objectMapper) {
        this.productServiceInterface = productServiceInterface;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Check basics of the users request before doing anything else
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        Map<String, Predicate<String>> requiredHeaders = new HashMap<>();
        requiredHeaders.put("User-Id", HandlerHelper.isUUID());
        requiredHeaders.put("Content-Type", HandlerHelper.isJsonContentType());
        requiredHeaders.put("Authorisation", HandlerHelper.anyValue());
        HandlerHelper.HeaderValidationResult headerResult = HandlerHelper.validateHeaders(exchange, requiredHeaders);
        if (!headerResult.isValid()) return;
        UUID userId = UUID.fromString(headerResult.getValues().get("User-Id"));

        String token = headerResult.getValues().get("Authorisation");
        if (!JwtUtil.validateToken(token)) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        // URL validation
        String path = exchange.getRequestURI().getPath();
        HandlerHelper.validateUrlNoId(path, "accounts", exchange);

        // Make GET call to DB to determine current state of Data
        List<ProductsTableResponseModel> dbResponse; 
        try {
            dbResponse = productServiceInterface.retrieveProductsFromDatabase(userId);
            String response = objectMapper.writeValueAsString(dbResponse);
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().flush();
            exchange.getResponseBody().close();
        } catch (SQLException | IOException e) {
            exchange.sendResponseHeaders(500, -1);
            System.err.println("DataBase Error : " + e.getMessage());
        }
    }
}
