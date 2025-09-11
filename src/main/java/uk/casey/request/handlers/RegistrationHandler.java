package uk.casey.request.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import uk.casey.models.RegistrationRequestModel;
import uk.casey.request.ProductService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class RegistrationHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        RegistrationRequestModel registrationRequestModel;
        ProductService productService = new ProductService();

        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String accept = exchange.getRequestHeaders().getFirst("Accept");
        if (accept == null || !accept.equals("application/json")) {
            exchange.sendResponseHeaders(400, -1);
            System.out.println("Missing or invalid Content-Type header");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        HandlerHelper.validateUrlNoId(path,"register", exchange);

        registrationRequestModel =  HandlerHelper.parseRequestBody(exchange, objectMapper, RegistrationRequestModel.class);
        if(registrationRequestModel == null) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        try {
            UUID userId = productService.registerWithDataBase(registrationRequestModel.getUsername(), registrationRequestModel.getPasscode(), registrationRequestModel.getEmail());
            String response = "{\"userId\": \"" + userId + "\"}";
            exchange.sendResponseHeaders(201, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().flush();
            exchange.getResponseBody().close();
        } catch (SQLException | IOException e) {
            exchange.sendResponseHeaders(500, -1);
            System.err.println("Error creating account in DataBase " + e.getMessage());
        }
    }
}
