package com.example.Music_management.exception;

/**
 * 上传的歌名已存在(music.title 全站唯一,同时决定 MinIO key)时抛出。
 * 由 MusicController 捕获并透传具体信息 + 记录日志(遵循项目错误处理约定)。
 */
public class MusicAlreadyExistException extends RuntimeException {
    public MusicAlreadyExistException(String s) {
        super(s);
    }
}
