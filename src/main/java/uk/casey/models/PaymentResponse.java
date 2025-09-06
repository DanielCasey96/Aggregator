package uk.casey.models;

import java.math.BigDecimal;

public class PaymentResponse {
    private Boolean processed;
    private PaymentRequest request;

    public PaymentResponse(
            Boolean processed, PaymentRequest request) {
        this.processed = processed;
        this.request = request;
    }


    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    public PaymentRequest getRequest() {
        return request;
    }

    public void setRequest(PaymentRequest request) {
        this.request = request;
    }

}
