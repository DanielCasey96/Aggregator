package uk.casey.request.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import uk.casey.request.ProductService;

import java.io.IOException;
import java.sql.SQLException;

public class RemoveProductHandler implements HttpHandler {

     @Override
     public void handle(HttpExchange exchange) throws IOException {
         ProductService productService = new ProductService();
         if (!"DELETE".equals(exchange.getRequestMethod())) {
             exchange.sendResponseHeaders(405, -1); // Method Not Allowed
             return;
         }

         String userId = exchange.getRequestHeaders().getFirst("UserId");
         if(!HandlerHelper.validateHeaders(exchange, userId)) {
             return;
         }

         String path = exchange.getRequestURI().getPath();
         String[] uriParts = path.split("/");
         if (uriParts.length != 3 || !uriParts[1].equals("remove-product")) {
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

         try {
             productService.removeProductFromDataBase(java.util.UUID.fromString(userId), id);
             exchange.sendResponseHeaders(204, -1);
             exchange.getResponseBody().flush();
             exchange.getResponseBody().close();
         } catch (SQLException e) {
             exchange.sendResponseHeaders(500, -1);
             System.err.println("Error updating DataBase " + e.getMessage());
         }
     }

 }