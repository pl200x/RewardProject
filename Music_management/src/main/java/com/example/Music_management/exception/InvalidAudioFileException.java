package com.example.Music_management.exception;

/**
 * 上传的音频文件不合法(为空/非 mp3/超过大小上限)或元数据不合法(歌名含非法字符等)时抛出。
 * 由 MusicController 捕获并透传具体信息 + 记录日志(遵循项目错误处理约定)。
 */
public class InvalidAudioFileException extends RuntimeException {
    public InvalidAudioFileException(String s) {
        super(s);
    }
}
