package com.example.Music_management.exception;

public class UserNotLoginException  extends RuntimeException {
    public UserNotLoginException (String s){
        super(s);
    }
}
