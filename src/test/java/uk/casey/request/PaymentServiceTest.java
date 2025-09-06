package uk.casey.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.casey.models.PaymentRequest;
import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    @Test
    void isBodyValid()  {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new java.math.BigDecimal("100.00"));
        paymentRequest.setPaymentType("CreditCard");
        paymentRequest.setCurrency("USD");

        PaymentService service = new PaymentService();
        assertTrue(service.paymentRequestBodyIsValid(paymentRequest));
    }

    @Test
    void bodyInvalidAmountNegative()  {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new java.math.BigDecimal("-100.00"));
        paymentRequest.setPaymentType("CreditCard");
        paymentRequest.setCurrency("USD");

        PaymentService service = new PaymentService();
        assertFalse(service.paymentRequestBodyIsValid(paymentRequest));
    }

    @Test
    void bodyInvalidAmountNull()  {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(null);
        paymentRequest.setPaymentType("CreditCard");
        paymentRequest.setCurrency("USD");

        PaymentService service = new PaymentService();
        assertFalse(service.paymentRequestBodyIsValid(paymentRequest));
    }

    @Test
    void bodyInvalidPaymentTypeNull()  {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new java.math.BigDecimal("100.00"));
        paymentRequest.setPaymentType(null);
        paymentRequest.setCurrency("USD");

        PaymentService service = new PaymentService();
        assertFalse(service.paymentRequestBodyIsValid(paymentRequest));
    }

    @Test
    void bodyInvalidPaymentTypeEmpty()  {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new java.math.BigDecimal("100.00"));
        paymentRequest.setPaymentType("");
        paymentRequest.setCurrency("USD");

        PaymentService service = new PaymentService();
        assertFalse(service.paymentRequestBodyIsValid(paymentRequest));
    }

    @Test
    void bodyInvalidCurrencyNull()  {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new java.math.BigDecimal("100.00"));
        paymentRequest.setPaymentType(null);
        paymentRequest.setCurrency(null);

        PaymentService service = new PaymentService();
        assertFalse(service.paymentRequestBodyIsValid(paymentRequest));
    }

    @Test
    void bodyInvalidCurrencyEmpty()  {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new java.math.BigDecimal("100.00"));
        paymentRequest.setPaymentType("CreditCard");
        paymentRequest.setCurrency("");

        PaymentService service = new PaymentService();
        assertFalse(service.paymentRequestBodyIsValid(paymentRequest));
    }

}
