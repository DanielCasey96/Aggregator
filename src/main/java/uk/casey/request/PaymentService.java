package uk.casey.request;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;

import uk.casey.models.AccountsTableResponse;
import uk.casey.models.PaymentRequest;

public class PaymentService {

    public boolean paymentRequestBodyIsValid(PaymentRequest request) {
        return request != null
                && request.getAmount() != null
                && request.getAmount().compareTo(BigDecimal.ZERO) > 0
                && request.getPaymentType() != null && !request.getPaymentType().trim().isEmpty()
                && request.getCurrency() != null && !request.getCurrency().trim().isEmpty();
    }

    public AccountsTableResponse retrieveAccountsFromDatabase() throws IOException, SQLException {
        System.out.println("Starting to gather data from DB");
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        }

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        String sql = "SELECT balance, debt, lastupdated, currency FROM accounts WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(url, username, password);
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, 1);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    AccountsTableResponse response = new AccountsTableResponse();
                    response.setBalance(rs.getBigDecimal("balance"));
                    response.setDebt(rs.getBigDecimal("debt"));
                    response.setLastUpdated(rs.getTimestamp("lastupdated"));
                    response.setCurrency(rs.getString("currency"));
                    return response;
                }
            }
        }
        return null;
    }

    private boolean calculateAccounts(AccountsTableResponse response, PaymentRequest request) {
        System.out.println("Data gathered from DB");
        System.out.println("Starting to calculate");

        if(!request.getCurrency().equals(response.getCurrency())) {
            System.out.println("Mismatch of Currencies");
            return false;
        }

        BigDecimal newDebt = response.getDebt().subtract(request.getAmount());
        BigDecimal newBalance = response.getBalance().subtract(newDebt);
        Timestamp serviceTimestamp = new java.sql.Timestamp(System.currentTimeMillis());

        response.setBalance(newBalance);
        response.setDebt(newDebt);
        response.setLastUpdated(serviceTimestamp);
        System.out.println("calculations done");

        return true;
    }

    public boolean savePaymentToDatabase(AccountsTableResponse response, PaymentRequest request) throws IOException {
        System.out.println("Starting to update DB");

        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        }

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        String sql = "UPDATE accounts SET balance = ?, debt = ?, lastupdated = ? WHERE id = ?";

        if(!calculateAccounts(response, request)) {
            System.out.println("Calculation failure, request rejected");
            return false;
        }

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBigDecimal(1, response.getBalance());
            statement.setBigDecimal(2, response.getDebt());
            statement.setTimestamp(3, response.getLastUpdated());
            statement.setInt(4, 1);

            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        } catch (Exception e) {
            System.out.println("Error Updating DataBase: " + e.getMessage());
            return false;
        }
    }
}
