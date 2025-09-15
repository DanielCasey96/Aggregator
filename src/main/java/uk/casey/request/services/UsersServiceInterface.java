package uk.casey.request.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public interface UsersServiceInterface {

    UUID registerWithDatabase(String customerUsername, String passcode, String email) throws IOException, SQLException;

    String getStoredPassword(UUID userId, String customerUsername) throws IOException, SQLException;

}
