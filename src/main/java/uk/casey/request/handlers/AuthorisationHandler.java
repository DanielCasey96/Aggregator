package uk.casey.request.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import uk.casey.models.LoginRequestModel;
import uk.casey.request.ProductService;
import uk.casey.utils.JwtUtil;

public class AuthorisationHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        LoginRequestModel loginRequestModel;
        ProductService productService = new ProductService();

        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String userIdStr = exchange.getRequestHeaders().getFirst("UserId");
        if (!HandlerHelper.validateHeaders(exchange, userIdStr)) {
            return;
        }
        UUID userId = UUID.fromString(userIdStr);

        String path = exchange.getRequestURI().getPath();
        boolean validUrl = HandlerHelper.validateUrlNoId(path, "authorise", exchange);
        if(!validUrl) {
            exchange.sendResponseHeaders(404, -1);
        }

        loginRequestModel = HandlerHelper.parseRequestBody(exchange, objectMapper, LoginRequestModel.class);
        if (loginRequestModel == null) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        try {
            boolean authenticated = productService.queryDataOfDataBase(userId, loginRequestModel.getUsername(), loginRequestModel.getPasscode());
            if (authenticated) {
                System.out.println("Is authenticated");
                String token = JwtUtil.generateToken(userId, loginRequestModel.getUsername());
                System.out.println("token " + token);
                exchange.sendResponseHeaders(200, token.getBytes().length);
                System.out.println("should be 200");
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
