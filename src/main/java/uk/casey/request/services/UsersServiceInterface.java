package uk.casey.request.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

public interface UsersServiceInterface {

    UUID registerWithDatabase(String customerUsername, String passcode, String email, Properties properties) throws IOException, SQLException;

    String getStoredPassword(UUID userId, String customerUsername, Properties properties) throws IOException, SQLException;

}
