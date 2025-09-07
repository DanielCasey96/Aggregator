package uk.casey.request.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import uk.casey.models.ProductsTableResponseModel;
import uk.casey.request.ProductService;

public class RetrievalHandler implements HttpHandler {
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductService productService = new ProductService();

        // Check basics of the users request before doing anything else
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        String ContentType = exchange.getRequestHeaders().getFirst("Content-Type");
        String UserId = exchange.getRequestHeaders().getFirst("UserId");
        if (ContentType == null || UserId == null || !ContentType.equals("application/json")) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
            System.out.println("Invalid Headers");
            return;
        }

        // Make GET call to DB to determine current state of Data
        List<ProductsTableResponseModel> dbResponse; 
        try {
            dbResponse = productService.retrieveProductsFromDatabase(java.util.UUID.fromString(UserId));
        } catch (SQLException e) {
            exchange.sendResponseHeaders(500, -1);
            System.err.println("DataBase Error : " + e.getMessage());
            return;
        }

            String response = objectMapper.writeValueAsString(dbResponse);
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().flush();
            exchange.getResponseBody().close();
    }
}
