package uk.casey.request.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import uk.casey.models.ProductRequestModel;
import uk.casey.request.services.ProductServiceInterface;
import uk.casey.utils.JwtUtil;

public class NewProductHandler implements HttpHandler {

    private final ProductServiceInterface productServiceInterface;
    private final Properties properties;
    private final ObjectMapper objectMapper;

    public NewProductHandler(ProductServiceInterface productServiceInterface, Properties properties, ObjectMapper objectMapper) {
        this.productServiceInterface = productServiceInterface;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        ProductRequestModel prm;

        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        String userIdStr = exchange.getRequestHeaders().getFirst("UserId");
        if(!HandlerHelper.validateHeaders(exchange, userIdStr)) {
            return;
        }

        String token = exchange.getRequestHeaders().getFirst("Authorisation");
        if (!JwtUtil.validateToken(token)) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        // URL validation
        String path = exchange.getRequestURI().getPath();
        HandlerHelper.validateUrlNoId(path, "add-product", exchange);

        // Parse the request body
        prm  = HandlerHelper.parseRequestBody(exchange, objectMapper, ProductRequestModel.class);

        try {
            productServiceInterface.createProductInDatabase(prm.getUserId(), prm.getName(), prm.getType(), prm.getProvider(), prm.getCategory(), prm.getValue(), prm.getUpdatedAt(), properties);
            exchange.sendResponseHeaders(201, -1);
            exchange.getResponseBody().flush();
            exchange.getResponseBody().close();
        } catch (SQLException | IOException | IllegalStateException e) {
            exchange.sendResponseHeaders(500, -1);
            System.err.println("Error updating DataBase " + e.getMessage());
        }
    }
}