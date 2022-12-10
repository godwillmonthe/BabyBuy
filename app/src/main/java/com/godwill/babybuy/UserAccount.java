package com.godwill.babybuy;

public class UserAccount {
    String fullNames, email, username;

    public UserAccount() {
    }

    public UserAccount(String fullNames, String email, String username) {
        this.fullNames = fullNames;
        this.email = email;
        this.username = username;
    }

    public String getFirstName() {
        return fullNames;
    }

    public void setFirstName(String firstName) {
        this.fullNames = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
