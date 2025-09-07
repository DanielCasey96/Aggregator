package uk.casey.models;

import java.math.BigDecimal;

public class ValueModel {

    private final BigDecimal value;

    public ValueModel(
        BigDecimal value) {
        this.value = value;
    }

    private boolean isValid() {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    public BigDecimal getValue() {
        if (!isValid()) {
            throw  new IllegalStateException("Value is not valid");
        }
        return value;
    }
}
