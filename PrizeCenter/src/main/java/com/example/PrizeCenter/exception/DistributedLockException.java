package com.example.PrizeCenter.exception;

public class DistributedLockException extends RuntimeException {
    public DistributedLockException(String message) {
        super(message);
    }
}
