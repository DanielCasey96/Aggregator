package uk.casey.models;

import java.math.BigDecimal;

public class PaymentRequest {
    private BigDecimal amount;
    private String paymentType;
    private String currency;

    public PaymentRequest(BigDecimal amount, String paymentType, String currency) {
        this.amount = amount;
        this.paymentType = paymentType;
        this.currency = currency;
    }

    public PaymentRequest() {
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentType() {
        return this.paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

}
