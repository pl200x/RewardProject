package com.example.Music_management.exception;

public class MusicNotExistException extends RuntimeException {
    public MusicNotExistException (String s){
        super(s);
    }
}
