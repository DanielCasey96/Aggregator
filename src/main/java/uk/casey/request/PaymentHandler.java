package uk.casey.request;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import uk.casey.models.AccountsTableResponse;
import uk.casey.models.PaymentRequest;

public class PaymentHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {

        PaymentService paymentService = new PaymentService();

        // Check basics of the users request before doing anything else
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }

        String ContentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (ContentType == null || !ContentType.equals("application/json")) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
            System.out.println("Missing or invalid Content-Type header");
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        PaymentRequest paymentRequest;

        InputStream requestBody = exchange.getRequestBody();
        StringBuilder body = new StringBuilder();
        int ch;
        while ((ch = requestBody.read()) != -1) {
            body.append((char) ch);
        }

        try {
            paymentRequest = objectMapper.readValue(body.toString(), PaymentRequest.class);
        } catch (Exception e) {
            exchange.sendResponseHeaders(400, -1);
            System.out.println("Invalid JSON format");
            return;
        }

        if (!paymentService.paymentRequestBodyIsValid(paymentRequest)) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        // Make GET call to DB to determine current state of Data
        AccountsTableResponse dbResponse; 
        try {
            dbResponse = paymentService.retrieveAccountsFromDatabase();
        } catch (SQLException e) {
            exchange.sendResponseHeaders(500, -1);
            System.err.println("DataBase Error : " + e.getMessage());
            return;
        }

        // Calculate values to send to DB
        // Make PATCH call to DB
        if (paymentService.savePaymentToDatabase(dbResponse, paymentRequest)) {
            System.out.println("DB updated");

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("processed", true);
            responseMap.putAll(objectMapper.convertValue(paymentRequest, Map.class));
            String response = objectMapper.writeValueAsString(responseMap);

            // Return response to the User
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().flush();
            exchange.getResponseBody().close();
        } else {
            exchange.sendResponseHeaders(500, -1); // Internal Server Error
        }
    }
}
