package uk.casey.models;

public class LoginRequestModel {

    private String username;
    private String passcode;

    public LoginRequestModel(
            String username,
            String passcode) {
        this.username = username;
        this.passcode = passcode;
    }

    public LoginRequestModel() {}

    public boolean isValid() {
        return isNotBlank(username) && isNotBlank(passcode);
    }

    private boolean isNotBlank(String item) {
        return item != null && !item.trim().isEmpty();
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
}
