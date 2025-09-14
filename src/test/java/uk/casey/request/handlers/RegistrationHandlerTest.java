package uk.casey.request.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import uk.casey.request.services.UsersServiceInterface;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RegistrationHandlerTest {

    private HttpExchange exchange;
    private UsersServiceInterface usersServiceInterface;

    @BeforeEach
    void setUp() {
        exchange = mock(HttpExchange.class);
        usersServiceInterface = mock(UsersServiceInterface.class);
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
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/register"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));

        when(usersServiceInterface.registerWithDatabase(
                anyString(), anyString(), anyString()
        )).thenReturn(UUID.randomUUID());

        RegistrationHandler handler = new RegistrationHandler(usersServiceInterface);
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
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/register"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));
        OutputStream responseBody = mock(OutputStream.class);
        when(exchange.getResponseBody()).thenReturn(responseBody);

        when(usersServiceInterface.registerWithDatabase(
                anyString(), anyString(), anyString()
        )).thenReturn(UUID.randomUUID());

        RegistrationHandler handler = new RegistrationHandler(usersServiceInterface);
        handler.handle(exchange);

        verify(responseBody).flush();
        verify(responseBody).close();
    }

    @Tag("unit-integration")
    @Test
    void returns405ForNonPostMethod() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("GET");

        RegistrationHandler handler = new RegistrationHandler(usersServiceInterface);
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

        RegistrationHandler handler = new RegistrationHandler(usersServiceInterface);
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

        RegistrationHandler handler = new RegistrationHandler(usersServiceInterface);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    //Remove this is not specific to the handler
    @Tag("unit-integration")
    @Test
    void validUrlReturnsSuccess() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        boolean result = HandlerHelper.validateUrlNoId("/register", "register", exchange);
        assertEquals(true, result);
        verify(exchange, never()).sendResponseHeaders(anyInt(), anyLong());
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
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/register"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));
        doThrow(new IOException("DB error IO")).when(usersServiceInterface).registerWithDatabase(
                anyString(), anyString(), anyString()
        );

        RegistrationHandler handler = new RegistrationHandler(usersServiceInterface);
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
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/register"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));
        doThrow(new SQLException("DB error IO")).when(usersServiceInterface).registerWithDatabase(
                anyString(), anyString(), anyString()
        );

        RegistrationHandler handler = new RegistrationHandler(usersServiceInterface);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(500, -1);
    }


}
