package uk.casey.models;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class AccountsTableResponse {

    private Integer id;
    private BigDecimal balance;
    private BigDecimal debt;
    private String currency;
    private Timestamp lastUpdated;

    AccountsTableResponse(
            Integer id,
            BigDecimal balance,
            BigDecimal debt,
            String currency,
            Timestamp lastUpdated) {
        this.id = id;
        this.balance = balance;
        this.debt = debt;
        this.currency = currency;
        this.lastUpdated = lastUpdated;
    }

    public AccountsTableResponse() {
    }

    public Integer getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal newBalance) {
        this.balance = newBalance;
    }

    public BigDecimal getDebt() {
        return debt;
    }

    public void setDebt(BigDecimal newDebt) {
        this.debt = newDebt;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String repsonseCurrency) {
        this.currency = repsonseCurrency;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp serviceTimestamp) {
        this.lastUpdated = serviceTimestamp;
    }

}
