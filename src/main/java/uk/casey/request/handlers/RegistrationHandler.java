package uk.casey.request.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import uk.casey.models.RegistrationRequestModel;
import uk.casey.request.services.UsersServiceInterface;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class RegistrationHandler extends HandlerHelper implements HttpHandler {

    private final UsersServiceInterface usersServiceInterface;
    private final ObjectMapper objectMapper;

    public RegistrationHandler(UsersServiceInterface usersServiceInterface, ObjectMapper objectMapper) {
        this.usersServiceInterface = usersServiceInterface;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        RegistrationRequestModel registrationRequestModel;

        if(!methodValidation(exchange, "POST")) return;

        Map<String, Predicate<String>> requiredHeaders = new HashMap<>();
        requiredHeaders.put("Accept", isJsonContentType());
        if (!validateHeaders(exchange, requiredHeaders).isValid()) return;

        validateUrlNoId(exchange.getRequestURI().getPath(),"register", exchange);

        if(parseRequestBody(exchange, objectMapper, RegistrationRequestModel.class) == null) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        String hashedPassword = BCrypt.hashpw(registrationRequestModel.getPasscode(), BCrypt.gensalt());

        try {
            UUID userId = usersServiceInterface.registerWithDatabase(registrationRequestModel.getUsername(), hashedPassword, registrationRequestModel.getEmail());
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
