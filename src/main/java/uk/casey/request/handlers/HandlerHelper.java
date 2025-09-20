package uk.casey.request.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import uk.casey.utils.JwtUtil;

abstract class HandlerHelper {

    protected boolean methodValidation(HttpExchange exchange, String expectedMethod) throws IOException {
        String actualMethod = exchange.getRequestMethod();
        if(actualMethod == null || !expectedMethod.equalsIgnoreCase(actualMethod)) {
            exchange.sendResponseHeaders(405, -1);
            System.out.println("Incorrect Method Type");
            return false;
        }
        return true;
    }

    protected boolean tokenHandling(HttpExchange exchange, String token) throws IOException {
        if(!JwtUtil.validateToken(token)) {
            exchange.sendResponseHeaders(401, -1);
            return false;
        }
        return true;
    }

    protected HeaderValidationResult validateHeaders(HttpExchange exchange, Map<String, Predicate<String>> requiredHeaders) throws IOException {
        Map<String, String> found = new HashMap<>();

        for(Map.Entry<String, Predicate<String>> entry : requiredHeaders.entrySet()) {
            String headerName = entry.getKey();
            Predicate<String> check = entry.getValue();
            String headerValueRaw = exchange.getRequestHeaders().getFirst(headerName);

            if(headerValueRaw == null) {
                String message = "Header missing "+ headerName;
                exchange.sendResponseHeaders(400, -1);
                return HeaderValidationResult.failure(400, message);
            }

            String headerValue = headerValueRaw.trim();

            if(check != null && !check.test(headerValue)) {
                String message = "Header format invalid " + headerName;
                exchange.sendResponseHeaders(400, -1);
                return HeaderValidationResult.failure(400, message);
            }

            found.put(headerName, headerValue);
        }

        return HeaderValidationResult.success(found);
    }

    protected Predicate<String> anyValue() {
        return Objects::nonNull;
    }

    protected Predicate<String> isJsonContentType() {
        return s -> s != null && s.toLowerCase().startsWith("application/json");
    }

    protected Predicate<String> isUUID() {
        return s -> {
            if (s == null) return false;
            try {
                UUID.fromString(s);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        };
    }

    protected Integer validateUrlWithId(String path, String endpoint, HttpExchange exchange) throws IOException {
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

    protected boolean validateUrlNoId(String path, String endpoint, HttpExchange exchange) throws IOException {
        String[] uriParts = path.split("/");
        if (uriParts.length == 2 && uriParts[1].equals(endpoint)) {
            return true;
        } else {
            exchange.sendResponseHeaders(404, -1);
            return false;
        }
    }

    protected <T> T parseRequestBody(HttpExchange exchange, ObjectMapper objectMapper, Class<T> clazz) throws IOException {
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

    public static final class HeaderValidationResult {
        private final boolean valid;
        private final int statusCode;
        private final String message;
        private final Map<String, String> values;

        private HeaderValidationResult(boolean valid, int statusCode, String message, Map<String, String> values) {
            this.valid = valid;
            this.statusCode = statusCode;
            this.message = message;
            this.values = values;
        }

        public static HeaderValidationResult success(Map<String, String> values) {
            return new HeaderValidationResult(true, 200, null, values);
        }

        public static HeaderValidationResult failure(int statusCode, String message) {
            return new HeaderValidationResult(false, statusCode, message, null);
        }

        public boolean isValid() { return valid; }
        public Map<String, String> getValues() { return values; }
    }
}
