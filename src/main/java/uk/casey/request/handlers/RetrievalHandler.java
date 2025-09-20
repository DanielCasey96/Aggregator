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

public class RetrievalHandler extends HandlerHelper implements HttpHandler {

    private final ProductServiceInterface productServiceInterface;
    private final ObjectMapper objectMapper;

    public RetrievalHandler(ProductServiceInterface productServiceInterface, ObjectMapper objectMapper) {
        this.productServiceInterface = productServiceInterface;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Check basics of the users request before doing anything else
        if(!methodValidation(exchange, "GET")) return;

        Map<String, Predicate<String>> requiredHeaders = new HashMap<>();
        requiredHeaders.put("User-Id", isUUID());
        requiredHeaders.put("Content-Type", isJsonContentType());
        requiredHeaders.put("Authorisation", anyValue());
        HeaderValidationResult headerResult = validateHeaders(exchange, requiredHeaders);
        if (!headerResult.isValid()) return;
        UUID userId = UUID.fromString(headerResult.getValues().get("User-Id"));

        if(!tokenHandling(exchange, headerResult.getValues().get("Authorisation"))) return;

        // URL validation
        String path = exchange.getRequestURI().getPath();
        validateUrlNoId(path, "accounts", exchange);

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
