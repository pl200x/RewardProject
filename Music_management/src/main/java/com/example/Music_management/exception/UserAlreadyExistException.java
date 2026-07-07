package com.example.Music_management.exception;

public class UserAlreadyExistException extends RuntimeException {
    public UserAlreadyExistException(String s){
        super(s);
    }
}
