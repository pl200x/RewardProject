package com.example.Music_management.exception;

/**
 * MinIO 中不存在该歌曲的音频对象(key = <title>.mp3)时抛出。
 * 由 MusicController 捕获并透传具体信息 + 记录日志(遵循项目错误处理约定)。
 */
public class AudioNotFoundException extends RuntimeException {
    public AudioNotFoundException(String s) {
        super(s);
    }
}
