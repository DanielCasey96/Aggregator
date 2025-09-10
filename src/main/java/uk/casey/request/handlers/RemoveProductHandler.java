package uk.casey.request.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import uk.casey.request.ProductService;

public class RemoveProductHandler implements HttpHandler {

    private final ProductService productService;

    public RemoveProductHandler(ProductService productService) {
        this.productService = productService;
    }

     @Override
     public void handle(HttpExchange exchange) throws IOException {
         if (!"DELETE".equals(exchange.getRequestMethod())) {
             exchange.sendResponseHeaders(405, -1); // Method Not Allowed
             return;
         }

        String userIdStr = exchange.getRequestHeaders().getFirst("UserId");
        if(!HandlerHelper.validateHeaders(exchange, userIdStr)) {
             return;
         }
         UUID userId = UUID.fromString(userIdStr);

         // URL validation
         String path = exchange.getRequestURI().getPath();
         int id = HandlerHelper.validateUrlWithId(path, "remove-product", exchange);
         if (id == -1) {
             return;
         }

         try {
             productService.removeProductFromDataBase(userId, id);
             exchange.sendResponseHeaders(204, -1);
             exchange.getResponseBody().flush();
             exchange.getResponseBody().close();
         } catch (SQLException | IOException e) {
             exchange.sendResponseHeaders(500, -1);
             System.err.println("Error updating DataBase " + e.getMessage());
         }
     }

 }