package uk.casey.request;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.casey.models.PaymentRequest;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PaymentsHandlerTest {

    @Test
    void bodyParsingFailureAmountMissing() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        String body = "{" +
                "\"amount\": ," +
                "\"paymentType\": \"CreditCard\"," +
                "\"currency\": \"USD\"" +
                "}";

        assertThrows(JsonParseException.class, () -> {
            objectMapper.readValue(body, PaymentRequest.class);
        });
    }

    @Test
    void bodyParsingFailureCurrencyMissing() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        String body = "{" +
                "\"amount\": -100.00," +
                "\"paymentType\": \"CreditCard\"," +
                "}";

        assertThrows(JsonParseException.class, () -> {
            objectMapper.readValue(body, PaymentRequest.class);
        });
    }
}
