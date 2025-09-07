package uk.casey.models;

import java.math.BigDecimal;

public class ValueModel {

    private BigDecimal value;

    public ValueModel(
        BigDecimal value) {
        this.value = value;
    }

    public ValueModel() {

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
