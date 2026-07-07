package com.example.Music_management.exception;

/**
 * 评论内容不合法(为空 / 超长)时抛出。
 * 由 CommentController 捕获并透传具体信息 + 记录日志(遵循项目错误处理约定)。
 */
public class InvalidCommentException extends RuntimeException {
    public InvalidCommentException(String s) {
        super(s);
    }
}
