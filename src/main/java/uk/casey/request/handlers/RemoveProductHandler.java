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

public class RemoveProductHandler extends HandlerHelper implements HttpHandler {

    private final ProductServiceInterface productServiceInterface;

    public RemoveProductHandler(ProductServiceInterface productServiceInterface) {
        this.productServiceInterface = productServiceInterface;
    }

     @Override
     public void handle(HttpExchange exchange) throws IOException {

         if(!methodValidation(exchange, "DELETE")) return;

         Map<String, Predicate<String>> requiredHeaders = new HashMap<>();
         requiredHeaders.put("User-Id", isUUID());
         requiredHeaders.put("Content-Type", isJsonContentType());
         requiredHeaders.put("Authorisation", anyValue());
         HeaderValidationResult headerResult = validateHeaders(exchange, requiredHeaders);
         if (!headerResult.isValid()) return;
         UUID userId = UUID.fromString(headerResult.getValues().get("User-Id"));

         if(!tokenHandling(exchange, headerResult.getValues().get("Authorisation"))) return;

         // URL validation
         String path = exchange.getRequestURI().getPath();
         int id = validateUrlWithId(path, "remove", exchange);
         if (id == -1) return;

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