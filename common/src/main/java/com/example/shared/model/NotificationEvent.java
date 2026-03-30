package com.example.shared.model;

public class NotificationEvent {

    private String accountId;
    private String message;

    public NotificationEvent() {
    }

    public NotificationEvent(String accountId, String message) {
        this.accountId = accountId;
        this.message = message;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
