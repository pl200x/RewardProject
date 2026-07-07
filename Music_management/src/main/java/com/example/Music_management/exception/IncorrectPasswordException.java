package com.example.Music_management.exception;

public class IncorrectPasswordException  extends RuntimeException {
    public IncorrectPasswordException(String s){
        super(s);
    }
}
