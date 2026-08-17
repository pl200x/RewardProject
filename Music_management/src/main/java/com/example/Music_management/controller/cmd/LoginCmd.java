package com.example.Music_management.controller.cmd;

public class LoginCmd {
    private String userName;
    private String password;

    public LoginCmd() {
    }

    public LoginCmd(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginCmd{" +
                "userName='" + userName + '\'' +
                ", password='[REDACTED]'" +
                '}';
    }
}
