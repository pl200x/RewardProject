package com.example.PrizeCenter.exception;

public class PrizeDoNotExistedException extends RuntimeException {
    public PrizeDoNotExistedException(String s) {
        super(s);
    }
}