package uk.casey.request.handlers;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import uk.casey.models.ValueModel;
import uk.casey.request.services.ProductServiceInterface;
import uk.casey.utils.JwtUtil;

public class UpdateProductHandler implements HttpHandler {

    private final ProductServiceInterface productServiceInterface;

    public UpdateProductHandler(ProductServiceInterface productServiceInterface) {
        this.productServiceInterface = productServiceInterface;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
         ObjectMapper objectMapper = new ObjectMapper();

        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        // Header validation
        String userIdStr = exchange.getRequestHeaders().getFirst("UserId");
        if(!HandlerHelper.validateHeaders(exchange, userIdStr)) {
            return;
        }
        UUID userId = UUID.fromString(userIdStr);

        String token = exchange.getRequestHeaders().getFirst("Authorisation");
        if (!JwtUtil.validateToken(token)) {
            exchange.sendResponseHeaders(401, -1);
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