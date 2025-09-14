package uk.casey.request.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public interface UsersServiceInterface {

    UUID registerWithDatabase(String customerUsername, String passcode, String email) throws IOException, SQLException;

    boolean queryDataOfDatabase(UUID userId, String customerUsername, String passcode) throws IOException, SQLException;

}
