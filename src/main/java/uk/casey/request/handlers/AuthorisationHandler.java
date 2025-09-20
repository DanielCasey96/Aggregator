package uk.casey.request.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.mindrot.jbcrypt.BCrypt;
import uk.casey.models.LoginRequestModel;
import uk.casey.request.services.ProductService;
import uk.casey.request.services.UsersServiceInterface;
import uk.casey.utils.JwtUtil;

public class AuthorisationHandler extends HandlerHelper implements HttpHandler {

    private final UsersServiceInterface usersServiceInterface;
    private final ObjectMapper objectMapper;

    public AuthorisationHandler(UsersServiceInterface usersServiceInterface, ObjectMapper objectMapper) {
        this.usersServiceInterface = usersServiceInterface;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        LoginRequestModel loginRequestModel;

        if(!methodValidation(exchange, "POST")) return;

        Map<String, Predicate<String>> requiredHeaders = new HashMap<>();
        requiredHeaders.put("User-Id", isUUID());
        requiredHeaders.put("Content-Type", isJsonContentType());
        HeaderValidationResult headerResult = validateHeaders(exchange, requiredHeaders);
        if (!headerResult.isValid()) return;

        UUID userId = UUID.fromString(headerResult.getValues().get("User-Id"));

        String path = exchange.getRequestURI().getPath();
        boolean validUrl = validateUrlNoId(path, "authorise", exchange);
        if(!validUrl) {
            exchange.sendResponseHeaders(404, -1);
        }

        loginRequestModel = parseRequestBody(exchange, objectMapper, LoginRequestModel.class);
        if (loginRequestModel == null) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        try {
            String storedHash = usersServiceInterface.getStoredPassword(userId, loginRequestModel.getUsername());

            if (storedHash == null) {
                exchange.sendResponseHeaders(401, -1);
                return;
            }

            boolean authenticated = BCrypt.checkpw(loginRequestModel.getPasscode(), storedHash);
            if (authenticated) {
                String token = JwtUtil.generateToken(userId, loginRequestModel.getUsername());
                exchange.sendResponseHeaders(200, token.getBytes().length);
                exchange.getResponseBody().write(token.getBytes());
                exchange.getResponseBody().flush();
                exchange.getResponseBody().close();
            } else {
                exchange.sendResponseHeaders(401, -1);
            }
        } catch (SQLException | IOException e) {
            exchange.sendResponseHeaders(500, -1);
        }
    }
}
