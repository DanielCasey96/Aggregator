package uk.casey.request.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import uk.casey.models.ProductRequestModel;
import uk.casey.models.RegistrationRequestModel;
import uk.casey.request.services.UsersServiceInterface;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RegistrationHandlerTest {

    private HttpExchange exchange;
    private UsersServiceInterface usersServiceInterface;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        exchange = mock(HttpExchange.class);
        usersServiceInterface = mock(UsersServiceInterface.class);
        objectMapper = mock(ObjectMapper.class);
    }

    @Tag("unit-integration")
    @Test
    void registerHandlerSuccess() throws IOException, SQLException {
        Headers headers = new Headers();
        headers.add("Accept", "application/json");

        String json = """
                {
                    "username" : "boogaloo2",
                    "passcode" : "electric",
                    "email" : "clear@yahoo.com"
                }
                """;

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/user/register"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));
        when(objectMapper.readValue(anyString(), eq(RegistrationRequestModel.class)))
                .thenReturn(new RegistrationRequestModel(
                        "boogaloo2",
                        "electric",
                        "clear@yahoo.com"));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));

        when(usersServiceInterface.registerWithDatabase(
                anyString(), anyString(), anyString()
        )).thenReturn(UUID.randomUUID());

        RegistrationHandler handler = new RegistrationHandler(usersServiceInterface, objectMapper);
        handler.handle(exchange);

        verify(exchange).getRequestMethod();
        verify(exchange, times(1)).getRequestHeaders();
        verify(exchange).sendResponseHeaders(201, 50);
    }

    @Tag("unit-integration")
    @Test
    void registerHandlerIsFlushedAndClosedAfterSuccess() throws IOException, SQLException {
        Headers headers = new Headers();
        headers.add("Accept", "application/json");

        String json = """
                {
                    "username" : "boogaloo2",
                    "passcode" : "electric",
                    "email" : "clear@yahoo.com"
                }
                """;

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/user/register"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));
        when(objectMapper.readValue(anyString(), eq(RegistrationRequestModel.class)))
                .thenReturn(new RegistrationRequestModel(
                        "boogaloo2",
                        "electric",
                        "clear@yahoo.com"));
        OutputStream responseBody = mock(OutputStream.class);
        when(exchange.getResponseBody()).thenReturn(responseBody);

        when(usersServiceInterface.registerWithDatabase(
                anyString(), anyString(), anyString()
        )).thenReturn(UUID.randomUUID());

        RegistrationHandler handler = new RegistrationHandler(usersServiceInterface, objectMapper);
        handler.handle(exchange);

        verify(responseBody).flush();
        verify(responseBody).close();
    }

    @Tag("unit-integration")
    @Test
    void returns405ForNonPostMethod() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("GET");

        RegistrationHandler handler = new RegistrationHandler(usersServiceInterface, objectMapper);
        handler.handle(exchange);

        verify(exchange).getRequestMethod();
        verify(exchange).sendResponseHeaders(405, -1);
        verifyNoMoreInteractions(exchange);
    }

    @Tag("unit-integration")
    @Test
    void returns400ForMissingAccept() throws IOException {
        Headers headers = new Headers();
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        RegistrationHandler handler = new RegistrationHandler(usersServiceInterface, objectMapper);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns400ForIncorrectAcceptValue() throws IOException {
        Headers headers = new Headers();
        headers.add("Accept", "application/txt");
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        RegistrationHandler handler = new RegistrationHandler(usersServiceInterface, objectMapper);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    //Remove this is not specific to the handler
    @Tag("unit-integration")
    @Test
    void validUrlReturnsSuccess() throws Exception {
        HandlerHelper helper = new HandlerHelper() {};
        HttpExchange exchange = mock(HttpExchange.class);
        boolean result = helper.validateUrlNoId("/user/register", "register", exchange);
        assertEquals(true, result);
        verify(exchange, never()).sendResponseHeaders(anyInt(), anyLong());
    }

    @Tag("unit-integration")
    @Test
    void returnsHashedPasswordSuccess() throws Exception {
        Headers headers = new Headers();
        headers.add("Accept", "application/json");
    String json = """
            {
                "username" : "boogaloo2",
                "passcode" : "password123",
                "email" : "clear@yahoo.com"
            }
            """;
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/user/register"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));
        when(objectMapper.readValue(anyString(), eq(RegistrationRequestModel.class)))
                .thenReturn(new RegistrationRequestModel(
                        "boogaloo2",
                        "password123",
                        "clear@yahoo.com"));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));

        when(usersServiceInterface.registerWithDatabase(
                anyString(), anyString(), anyString()
        )).thenReturn(UUID.randomUUID());

        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);

        RegistrationHandler handler = new RegistrationHandler(usersServiceInterface, objectMapper);
        handler.handle(exchange);

        verify(usersServiceInterface).registerWithDatabase(
                eq("boogaloo2"),
                passwordCaptor.capture(),
                eq("clear@yahoo.com"));
        String actualHashedPassword = passwordCaptor.getValue();
        assertTrue(actualHashedPassword.startsWith("$2a$"),
                "Password should be hashed with BCrypt");
        assertTrue(BCrypt.checkpw("password123", actualHashedPassword),
                "BCrypt hash should verify against the original password");
    }

    @Tag("unit-integration")
    @Test
    void returnsHashedPasswordSuccessMultiplePasswords() throws Exception {
        Headers headers = new Headers();
        headers.add("Accept", "application/json");
        String[] testPasswords = {"password", "123456", "!@#$%^&*", "", "veryLongPasswordWithSpecialChars123!@#"};
        for (String password : testPasswords) {
            // Reset mocks for each iteration
            reset(exchange, usersServiceInterface);

            String json = String.format(
                    "{\"username\": \"user\", \"passcode\": \"%s\", \"email\": \"test@example.com\"}",
                    password
            );

            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/user/register"));
            when(exchange.getRequestHeaders()).thenReturn(headers);
            when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));
            when(objectMapper.readValue(anyString(), eq(RegistrationRequestModel.class)))
                    .thenReturn(new RegistrationRequestModel(
                            "boogaloo2",
                            password,
                            "clear@yahoo.com"));
            when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));

            when(usersServiceInterface.registerWithDatabase(
                    anyString(), anyString(), anyString()
            )).thenReturn(UUID.randomUUID());

            ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);

            RegistrationHandler handler = new RegistrationHandler(usersServiceInterface, objectMapper);
            handler.handle(exchange);

            verify(usersServiceInterface).registerWithDatabase(anyString(), passwordCaptor.capture(), anyString());

            String actualHashedPassword = passwordCaptor.getValue();
            assertTrue(actualHashedPassword.startsWith("$2a$"));
            assertTrue(BCrypt.checkpw(password, actualHashedPassword),
                    String.format("BCrypt verification failed for password: %s", password));
        }
    }

    @Tag("unit-integration")
    @Test
    void returns500WhenDatabaseUpdateFailsIOException() throws Exception {
        Headers headers = new Headers();
        headers.add("Accept", "application/json");

        String json = """
                {
                    "username" : "boogaloo2",
                    "passcode" : "electric",
                    "email" : "clear@yahoo.com"
                }
                """;

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/user/register"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));
        when(objectMapper.readValue(anyString(), eq(RegistrationRequestModel.class)))
                .thenReturn(new RegistrationRequestModel(
                        "boogaloo2",
                        "electric",
                        "clear@yahoo.com"));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));
        doThrow(new IOException("DB error IO")).when(usersServiceInterface).registerWithDatabase(
                anyString(), anyString(), anyString()
        );

        RegistrationHandler handler = new RegistrationHandler(usersServiceInterface, objectMapper);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(500, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns500WhenDatabaseUpdateFailsSQLException() throws Exception {
        Headers headers = new Headers();
        headers.add("Accept", "application/json");

        String json = """
                {
                    "username" : "boogaloo2",
                    "passcode" : "electric",
                    "email" : "clear@yahoo.com"
                }
                """;

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/user/register"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));
        when(objectMapper.readValue(anyString(), eq(RegistrationRequestModel.class)))
                .thenReturn(new RegistrationRequestModel(
                        "boogaloo2",
                        "electric",
                        "clear@yahoo.com"));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));
        doThrow(new SQLException("DB error IO")).when(usersServiceInterface).registerWithDatabase(
                anyString(), anyString(), anyString()
        );

        RegistrationHandler handler = new RegistrationHandler(usersServiceInterface, objectMapper);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(500, -1);
    }


}
