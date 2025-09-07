package uk.casey.request.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import uk.casey.models.ValueModel;
import uk.casey.request.ProductService;

public class UpdateProductHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
         ObjectMapper objectMapper = new ObjectMapper();
         ProductService productService = new ProductService();

        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }
        
        String ContentType = exchange.getRequestHeaders().getFirst("Content-Type");
        String UserId = exchange.getRequestHeaders().getFirst("UserId");
        if (ContentType == null || UserId == null || !ContentType.equals("application/json")) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
            System.out.println("Missing or invalid Content-Type header");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] uriParts = path.split("/");
        if (uriParts.length != 3 || !uriParts[1].equals("update-value")) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        int id;
        try {
            id = Integer.parseInt(uriParts[2]);
        } catch (NumberFormatException e) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }
       
        ValueModel valueModel;

        InputStream requestBody = exchange.getRequestBody();
        StringBuilder body = new StringBuilder();
        int ch;
        while ((ch = requestBody.read()) != -1) {
            body.append((char) ch);
        }

        try {
            valueModel = objectMapper.readValue(body.toString(), ValueModel.class);
        } catch (Exception e) {
            exchange.sendResponseHeaders(400, -1);
            System.out.println("Invalid JSON format");
            return;
        }

        BigDecimal newValue;
        try {
            newValue = valueModel.getValue();
        } catch (IllegalStateException e) {
            exchange.sendResponseHeaders(400, -1);
            System.out.println("Invalid value: " + e.getMessage());
            return;
        }

        try {
            productService.updateProductToDatabase(newValue, id, java.util.UUID.fromString(UserId));
            exchange.sendResponseHeaders(204, -1);
            exchange.getResponseBody().flush();
            exchange.getResponseBody().close();
        } catch (SQLException e) {
            exchange.sendResponseHeaders(500, -1);
            System.out.println("Error updating DataBase " + e.getMessage());
        }
    }

}