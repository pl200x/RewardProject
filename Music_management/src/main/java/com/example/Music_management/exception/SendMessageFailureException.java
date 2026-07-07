package com.example.Music_management.exception;

public class SendMessageFailureException extends RuntimeException {
    public SendMessageFailureException(String s) {
        super(s);
    }
}