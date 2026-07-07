package com.example.Music_management.service;

import com.example.Music_management.controller.cmd.MusicCmd;
import org.springframework.web.multipart.MultipartFile;

public interface MusicAudioService {
    /**
     * 返回该歌曲音频(MinIO 对象 <title>.mp3)的 presigned GET URL,前端 <audio> 直接播放。
     * 歌曲不存在抛 MusicNotExistException;音频对象缺失抛 AudioNotFoundException。
     */
    String getAudioUrl(int musicId);

    /**
     * 上传音乐(方案A:后端中转):校验 → 音频 putObject 进 MinIO(key=<title>.mp3)
     * → 复用 addMusic 落库+发 Kafka(消费端进榜/按 tags 发通知邮件) → 返回新歌 id。
     * 落库失败时删除已上传对象补偿。
     * 歌名重复抛 MusicAlreadyExistException;文件/元数据非法抛 InvalidAudioFileException。
     */
    int uploadMusic(MusicCmd cmd, MultipartFile file);
}
