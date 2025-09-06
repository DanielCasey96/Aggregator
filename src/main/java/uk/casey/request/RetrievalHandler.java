package uk.casey.request;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import uk.casey.models.ProductsTableResponse;

public class RetrievalHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductService productService = new ProductService();
        ProductsTableResponse productsTableResponse = new ProductsTableResponse();

        // Check basics of the users request before doing anything else
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }

        String ContentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (ContentType == null || !ContentType.equals("application/json")) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
            System.out.println("Missing or invalid Content-Type header");
            return;
        }

        // Make GET call to DB to determine current state of Data
        List<ProductsTableResponse> dbResponse; 
        try {
            dbResponse = productService.retrieveProductsFromDatabase(1234, Arrays.asList(3));
        } catch (SQLException e) {
            exchange.sendResponseHeaders(500, -1);
            System.err.println("DataBase Error : " + e.getMessage());
            return;
        }

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("processed", true);
            responseMap.putAll(objectMapper.convertValue(productsTableResponse, Map.class));
            String response = objectMapper.writeValueAsString(responseMap);

            // Return response to the User
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().flush();
            exchange.getResponseBody().close();
    }
}
