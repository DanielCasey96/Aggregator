package uk.casey.request.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;

public class HandlerHelper {

    public static boolean validateHeaders(HttpExchange exchange, String userId) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.equals("application/json")) {
            exchange.sendResponseHeaders(400, -1);
            System.out.println("Missing or invalid Content-Type header");
            return false;
        }
        if (userId == null) {
            exchange.sendResponseHeaders(400, -1);
            System.out.println("Missing UserId header");
            return false;
        }
        try {
            java.util.UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            exchange.sendResponseHeaders(400, -1);
            System.out.println("Invalid UserId format: " + userId);
            return false;
        }
        return true;
    }

    public static Integer validateUrlWithId(String path, String endpoint, HttpExchange exchange) throws IOException {
        String[] uriParts = path.split("/");
        if (uriParts.length == 3 && uriParts[1].equals(endpoint)) {
            try {
                return Integer.parseInt(uriParts[2]);
            } catch (NumberFormatException e) {
                exchange.sendResponseHeaders(400, -1);
                return -1;
            }
        } else {
            exchange.sendResponseHeaders(404, -1);
            return -1;
        }
    }

    public static boolean validateUrlNoId(String path, String endpoint, HttpExchange exchange) throws IOException {
        String[] uriParts = path.split("/");
        if (uriParts.length == 2 && uriParts[1].equals(endpoint)) {
            return true;
        } else {
            exchange.sendResponseHeaders(404, -1);
            return false;
        }
    }

    public static <T> T parseRequestBody(HttpExchange exchange, ObjectMapper objectMapper, Class<T> clazz) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        if(requestBody == null) {
            exchange.sendResponseHeaders(400, -1);
            return null;
        }
        StringBuilder body = new StringBuilder();
        int ch;
        while ((ch = requestBody.read()) != -1) {
            body.append((char) ch);
        }

        try {
            return objectMapper.readValue(body.toString(), clazz);
        } catch (Exception e) {
            exchange.sendResponseHeaders(400, -1);
            System.out.println("Invalid JSON format");
            return null;
        }
    }
}
