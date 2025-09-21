package uk.casey.request.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import uk.casey.models.ProductRequestModel;
import uk.casey.utils.JwtUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

class HandlerHelperTest {
    private HttpExchange exchange;
    private HandlerHelper handlerHelper;

    @BeforeEach
    void setUp() {
        exchange = mock(HttpExchange.class);
        handlerHelper = new HandlerHelper() {};
    }

    @Test
    void methodValidationReturnsTrueForValidMethod() throws IOException {
        String passedMethod = "GET";
        when(exchange.getRequestMethod()).thenReturn("GET");

        assertTrue(handlerHelper.methodValidation(exchange, passedMethod));
    }

    @Test
    void methodValidationReturnsFalseForMethodMissmatch() throws IOException {
        String passedMethod = "PATCH";
        when(exchange.getRequestMethod()).thenReturn("GET");

        assertFalse(handlerHelper.methodValidation(exchange, passedMethod));
    }

    @Test
    void methodValidationReturns405ForMethodMissmatch() throws IOException {
        String passedMethod = "PATCH";
        when(exchange.getRequestMethod()).thenReturn("GET");

        handlerHelper.methodValidation(exchange, passedMethod);

        verify(exchange).sendResponseHeaders(405, -1);
    }

    @Test
    void methodValidationReturnsFalseForMethodNull() throws IOException {
        String passedMethod = "GET";
        when(exchange.getRequestMethod()).thenReturn(null);

        boolean output = handlerHelper.methodValidation(exchange, passedMethod);
        assertFalse(output);
    }

    @Test
    void tokenHandlingReturnsTrueForValidToken() throws IOException {
        String passedToken = "eyacvbtgs";

        try(MockedStatic<JwtUtil> jwtUtil = mockStatic(JwtUtil.class)) {
            jwtUtil.when(() -> JwtUtil.validateToken(passedToken)).thenReturn(true);

            assertTrue(handlerHelper.tokenHandling(exchange, passedToken));
        }
    }

    @Test
    void tokenHandlingReturnsFalseforInvalidToken() throws IOException {
        String passedToken = "invalidToken";

        try (MockedStatic<JwtUtil> jwtUtil = mockStatic(JwtUtil.class)) {
            jwtUtil.when(() -> JwtUtil.validateToken(passedToken)).thenReturn(false);

            assertFalse(handlerHelper.tokenHandling(exchange, passedToken));
        }
    }

    @Test
    void tokenHandlingReturns401ForInvalidToken() throws IOException {
        String passedToken = "invalidToken";

        try (MockedStatic<JwtUtil> jwtUtil = mockStatic(JwtUtil.class)) {
            jwtUtil.when(() -> JwtUtil.validateToken(passedToken)).thenReturn(false);

            handlerHelper.tokenHandling(exchange, passedToken);

            verify(exchange).sendResponseHeaders(401, -1);
        }
    }

    @Test
    void anyValuePredicateReturnsTrueWhenNotNullValuePassed() {
        String value = "Android";

        assertTrue(handlerHelper.anyValue().test(value));
    }

    @Test
    void anyValuePredicateReturnsFalseWhenNullValuePassed() {

        assertFalse(handlerHelper.anyValue().test(null));
    }

    @Test
    void isJsonContentTypeReturnsTrueWhenValidValuePassed() {
        String value = "application/json";

        assertTrue(handlerHelper.isJsonContentType().test(value));
    }

    @Test
    void isJsonContentTypeReturnsFalseWhenInvalidValuePassed() {
        String value = "application/pdf";

        assertFalse(handlerHelper.isJsonContentType().test(value));
    }

    @Test
    void isJsonContentTypeReturnsFalseWhenNullValuePassed() {

        assertFalse(handlerHelper.isJsonContentType().test(null));
    }

    @Test
    void isUUIDReturnsTrueWhenValidUUIDPassed() {
        String value = UUID.randomUUID().toString();

        assertTrue(handlerHelper.isUUID().test(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "asd",
            "1234",
            "not-a-uuid",
            "00000000-0000-0000-0000-00000000000G",
            ""
    })
    void isUUIDReturnsFalseWhenInvalidUUIDPassed(String invalidUUID) {

        assertFalse(handlerHelper.isUUID().test(invalidUUID));
    }

    @Test
    void isUUIDReturnsFalseWhenNullValuePassed() {

        assertFalse(handlerHelper.isUUID().test(null));
    }

    @Test
    void validateUrlWithIdReturnsIdValueSuccess() throws IOException {
        String path = "/type/option/1";
        String endpoint = "option";

        int id = handlerHelper.validateUrlWithId(path, endpoint, exchange);

        assertEquals(1, id);
    }

    @Test
    void validateUrlWithNonIntIdReturnsFailure() throws IOException {
        String path = "/type/option/word";
        String endpoint = "option";

        int id = handlerHelper.validateUrlWithId(path, endpoint, exchange);

        assertEquals(-1, id);
        verify(exchange).sendResponseHeaders(400, -1);
    }

    @Test
    void validateUrlWithMissMatchedEndpointReturnsFailure() throws IOException {
        String path = "/type/option/1";
        String endpoint = "chicken";

        int id = handlerHelper.validateUrlWithId(path, endpoint, exchange);

        assertEquals(-1, id);
        verify(exchange).sendResponseHeaders(404, -1);
    }

    @Test
    void validateUrlWithMissingIdReturnsFailure() throws IOException {
        String path = "/type/option/";
        String endpoint = "option";

        int id = handlerHelper.validateUrlWithId(path, endpoint, exchange);

        assertEquals(-1, id);
        verify(exchange).sendResponseHeaders(404, -1);
    }

    @Test
    void validateUrlWithNoIdReturnsSuccess() throws IOException {
        String path = "/type/option";
        String endpoint = "option";

        boolean validation = handlerHelper.validateUrlNoId(path, endpoint, exchange);

        assertTrue(validation);
    }

    @Test
    void validateUrlWithNoIdAndMissMatchedPathReturnsFailure() throws IOException {
        String path = "/type/option";
        String endpoint = "chicken";

        boolean validation = handlerHelper.validateUrlNoId(path, endpoint, exchange);

        assertFalse(validation);
        verify(exchange).sendResponseHeaders(404, -1);
    }

    @Test
    void validateUrlTooManySegmentsReturnsFailure() throws IOException {
        String path = "/type/option/extra";
        String endpoint = "option";

        boolean validation = handlerHelper.validateUrlNoId(path, endpoint, exchange);

        assertFalse(validation);
        verify(exchange).sendResponseHeaders(404, -1);
    }

    @Test
    void parseRequestBodyReturnsSuccess() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = """
    {
        "userId": "12341234-1234-1234-1234-123412341234",
        "name": "Holiday Pot",
        "type": "Savings",
        "provider": "Lloyds",
        "category": null,
        "value": 78.24,
        "updatedAt": "2024-06-10T13:34:56.000Z"
    }
    """;

        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(json.getBytes()));

        ProductRequestModel result = handlerHelper.parseRequestBody(exchange, objectMapper, ProductRequestModel.class);

        assertNotNull(result);
        assertEquals("Holiday Pot", result.getName());
        assertEquals(BigDecimal.valueOf(78.24), result.getValue());
        verify(exchange, never()).sendResponseHeaders(anyInt(), anyLong());
    }

    @Test
    void parseRequestBodyReturnsNullForNullInputBody() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        when(exchange.getRequestBody()).thenReturn(null);

        assertNull(handlerHelper.parseRequestBody(exchange, objectMapper, ProductRequestModel.class));
        verify(exchange).sendResponseHeaders(400, -1);
    }

}
