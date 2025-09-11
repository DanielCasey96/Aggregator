package uk.casey.models;

public class RegistrationRequestModel {

    private String username;
    private String passcode;
    private String email;

    RegistrationRequestModel(
            String username,
            String passcode,
            String email) {
        this.username = username;
        this.passcode = passcode;
        this.email = email;
    }

    public RegistrationRequestModel() {}

    public boolean isValid() {
        return isNotBlank(username) && isNotBlank(passcode) && isValidEmail(email);
    }

    private boolean isNotBlank(String item) {
        return item != null && !item.trim().isEmpty();
    }

    private boolean isValidEmail(String email) {
        if (!isNotBlank(email)) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasscode() {
        return this.passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
