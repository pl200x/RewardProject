package com.example.Music_management.exception;

public class UserNotExistException extends RuntimeException {
    public UserNotExistException (String s){
        super(s);
    }
}
