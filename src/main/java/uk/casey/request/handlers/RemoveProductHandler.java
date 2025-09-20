package uk.casey.request.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Predicate;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

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

         Map<String, Predicate<String>> requiredHeaders = new HashMap<>();
         requiredHeaders.put("User-Id", HandlerHelper.isUUID());
         requiredHeaders.put("Content-Type", HandlerHelper.isJsonContentType());
         requiredHeaders.put("Authorisation", HandlerHelper.anyValue());
         HandlerHelper.HeaderValidationResult headerResult = HandlerHelper.validateHeaders(exchange, requiredHeaders);
         if (!headerResult.isValid()) return;
         UUID userId = UUID.fromString(headerResult.getValues().get("User-Id"));

         String token = headerResult.getValues().get("Authorisation");
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