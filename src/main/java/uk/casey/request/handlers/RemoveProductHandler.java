package uk.casey.request.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import uk.casey.request.ProductService;

import java.io.IOException;
import java.sql.SQLException;

public class RemoveProductHandler implements HttpHandler {

     @Override
     public void handle(HttpExchange exchange) throws IOException {
         ObjectMapper objectMapper = new ObjectMapper();
         ProductService productService = new ProductService();
         if (!"DELETE".equals(exchange.getRequestMethod())) {
             exchange.sendResponseHeaders(405, -1); // Method Not Allowed
             return;
         }

         String ContentType = exchange.getRequestHeaders().getFirst("Content-Type");
         if (ContentType == null || !ContentType.equals("application/json")) {
             exchange.sendResponseHeaders(400, -1); // Bad Request
             System.out.println("Missing or invalid Content-Type header");
             return;
         }

         try {
             productService.removeProductFromDataBase(java.util.UUID.fromString("12341234-1234-1234-1234-123412341234"), 3);
             exchange.sendResponseHeaders(204, -1);
             exchange.getResponseBody().flush();
             exchange.getResponseBody().close();
         } catch (SQLException e) {
             exchange.sendResponseHeaders(500, -1);
             System.err.println("Error updating DataBase " + e.getMessage());
         }
     }

 }