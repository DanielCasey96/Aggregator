package uk.casey.request.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import uk.casey.request.services.UsersServiceInterface;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class AuthorisationHandlerTest {

    private HttpExchange exchange;
    private UsersServiceInterface usersServiceInterface;
    private Properties properties;

    @BeforeEach
    void setUp() {
        exchange = mock(HttpExchange.class);
        usersServiceInterface = mock(UsersServiceInterface.class);
        properties = mock(Properties.class);
    }

    @Tag("unit-integration")
    @Test
    void authorisationHandlerSuccess() throws IOException, SQLException {
        Headers headers = new Headers();
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        headers.add("Content-Type", "application/json");
        String storedHash = BCrypt.hashpw("fatty", BCrypt.gensalt());

        String json = """
                {
                    "username" : "casey2boogaloo",
                    "passcode" : "fatty"
                }
                """;

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/authorise"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));

        when(usersServiceInterface.getStoredPassword(
                any(UUID.class), anyString(), any(Properties.class)
        )).thenReturn(storedHash);

        AuthorisationHandler handler = new AuthorisationHandler(usersServiceInterface, properties);
        handler.handle(exchange);

        verify(exchange).getRequestMethod();
        verify(exchange, times(2)).getRequestHeaders();
        verify(exchange).sendResponseHeaders(200, 209);
    }

    @Tag("unit-integration")
    @Test
    void authorisationHandlerIsFlushedAndClosed() throws IOException, SQLException {
        Headers headers = new Headers();
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        headers.add("Content-Type", "application/json");
        String storedHash = BCrypt.hashpw("fatty", BCrypt.gensalt());

        String json = """
                {
                    "username" : "casey2boogaloo",
                    "passcode" : "fatty"
                }
                """;

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/authorise"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));
        OutputStream responseBody = mock(OutputStream.class);
        when(exchange.getResponseBody()).thenReturn(responseBody);

        when(usersServiceInterface.getStoredPassword(
                any(UUID.class), anyString(), any(Properties.class)
        )).thenReturn(storedHash);

        AuthorisationHandler handler = new AuthorisationHandler(usersServiceInterface, properties);
        handler.handle(exchange);

        verify(responseBody).flush();
        verify(responseBody).close();
    }

    @Tag("unit-integration")
    @Test
    void returns405ForNonPostMethod() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("GET");

        AuthorisationHandler handler = new AuthorisationHandler(usersServiceInterface, properties);
        handler.handle(exchange);

        verify(exchange).getRequestMethod();
        verify(exchange).sendResponseHeaders(405, -1);
        verifyNoMoreInteractions(exchange);
    }

    @Tag("unit-integration")
    @Test
    void returns400ForMissingContentType() throws IOException {
        Headers headers = new Headers();
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        AuthorisationHandler handler = new AuthorisationHandler(usersServiceInterface, properties);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns400ForContentTypeValueIncorrect() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/txt");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        AuthorisationHandler handler = new AuthorisationHandler(usersServiceInterface, properties);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns400ForMissingUserId() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        AuthorisationHandler handler = new AuthorisationHandler(usersServiceInterface, properties);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns400ForUserIdIncorrectValue() throws IOException {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "notAUUIDFormat");
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestHeaders()).thenReturn(headers);

        AuthorisationHandler handler = new AuthorisationHandler(usersServiceInterface, properties);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns500WhenDatabaseUpdateFailsIOException() throws Exception {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");

        String json = """
                {
                    "username" : "casey2boogaloo",
                    "passcode" : "fatty"
                }
                """;

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/authorise"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));
        doThrow(new IOException("DB error IO")).when(usersServiceInterface).getStoredPassword(
                any(UUID.class), anyString(), any(Properties.class)
        );

        AuthorisationHandler handler = new AuthorisationHandler(usersServiceInterface, properties);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(500, -1);
    }

    @Tag("unit-integration")
    @Test
    void returns500WhenDatabaseUpdateFailsSQLException() throws Exception {
        Headers headers = new Headers();
        headers.add("Content-Type", "application/json");
        headers.add("UserId", "123e4567-e89b-12d3-a456-426614174000");

        String json = """
                {
                    "username" : "casey2boogaloo",
                    "passcode" : "fatty"
                }
                """;

        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(java.net.URI.create("/authorise"));
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));
        when(exchange.getResponseBody()).thenReturn(mock(OutputStream.class));
        doThrow(new SQLException("DB error IO")).when(usersServiceInterface).getStoredPassword(
                any(UUID.class), anyString(), any(Properties.class)
        );

        AuthorisationHandler handler = new AuthorisationHandler(usersServiceInterface, properties);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(500, -1);
    }

}
