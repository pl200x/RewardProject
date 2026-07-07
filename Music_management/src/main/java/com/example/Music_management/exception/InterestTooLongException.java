package com.example.Music_management.exception;

/**
 * 兴趣标签字符串过长(超过 user.interest 列上限)时抛出。
 * 由 UserController 捕获并透传具体信息 + 记录日志,避免压成通用的 "other unknown error"。
 */
public class InterestTooLongException extends RuntimeException {
    public InterestTooLongException(String s) {
        super(s);
    }
}
