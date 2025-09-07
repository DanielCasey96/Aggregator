package uk.casey.request.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import uk.casey.models.ProductRequestModel;
import uk.casey.request.ProductService;

public class NewProductHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductService productService = new ProductService();

        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }
        
        String ContentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (ContentType == null || !ContentType.equals("application/json")) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
            System.out.println("Missing or invalid Content-Type header");
            return;
        }

        ProductRequestModel prm;

        InputStream requestBody = exchange.getRequestBody();
        StringBuilder body = new StringBuilder();
        int ch;
        while ((ch = requestBody.read()) != -1) {
            body.append((char) ch);
        }

        try {
            prm = objectMapper.readValue(body.toString(), ProductRequestModel.class);
        } catch (Exception e) {
            exchange.sendResponseHeaders(400, -1);
            System.out.println("Invalid JSON format");
            return;
        }

        //Add more of the validation once the model i fleshed out
        try {
            prm.getType();
        } catch (IllegalStateException e) {
            exchange.sendResponseHeaders(400, -1);
            System.out.println("Invalid value: " + e.getMessage());
            return;
        }

        try {
            productService.createProductInDataBase("12341234-1234-1234-1234-123412341234", prm.getName(), prm.getType(), prm.getProvider(), prm.getCategory(), prm.getValue(), prm.getUpdatedAt());
            exchange.sendResponseHeaders(201, -1);
            exchange.getResponseBody().flush();
            exchange.getResponseBody().close();
        } catch (SQLException e) {
            exchange.sendResponseHeaders(500, -1);
            System.err.println("Error updating DataBase " + e.getMessage());
        }

    }
}