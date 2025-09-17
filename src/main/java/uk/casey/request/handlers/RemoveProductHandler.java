package uk.casey.request.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import uk.casey.request.services.ProductService;
import uk.casey.request.services.ProductServiceInterface;
import uk.casey.utils.JwtUtil;

public class RemoveProductHandler implements HttpHandler {

    private final ProductServiceInterface productServiceInterface;

    public RemoveProductHandler(ProductServiceInterface productServiceInterface) {
        this.productServiceInterface = productServiceInterface;
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

         String token = exchange.getRequestHeaders().getFirst("Authorisation");
         if (!JwtUtil.validateToken(token)) {
             exchange.sendResponseHeaders(401, -1);
             return;
         }

         // URL validation
         String path = exchange.getRequestURI().getPath();
         int id = HandlerHelper.validateUrlWithId(path, "remove-product", exchange);
         if (id == -1) {
             return;
         }

         try {
             productServiceInterface.removeProductFromDatabase(userId, id);
             exchange.sendResponseHeaders(204, -1);
             exchange.getResponseBody().flush();
             exchange.getResponseBody().close();
         } catch (SQLException | IOException e) {
             exchange.sendResponseHeaders(500, -1);
             System.err.println("Error updating DataBase " + e.getMessage());
         }
     }

 }