package uk.casey.request.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import uk.casey.models.ProductsTableResponseModel;
import uk.casey.request.ProductService;

public class RetrievalHandler implements HttpHandler {

    private final ProductService productService;

    public RetrievalHandler(ProductService productService) {
        this.productService = productService;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Check basics of the users request before doing anything else
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        String userId = exchange.getRequestHeaders().getFirst("UserId");
        if(!HandlerHelper.validateHeaders(exchange, userId)) {
            return;
        }

        // URL validation
        String path = exchange.getRequestURI().getPath();
        HandlerHelper.validateUrlNoId(path, "accounts", exchange);

        // Make GET call to DB to determine current state of Data
        List<ProductsTableResponseModel> dbResponse; 
        try {
            dbResponse = productService.retrieveProductsFromDatabase(java.util.UUID.fromString(userId));
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
