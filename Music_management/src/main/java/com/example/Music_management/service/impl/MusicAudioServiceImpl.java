package com.example.Music_management.service.impl;

import com.example.Music_management.controller.cmd.MusicCmd;
import com.example.Music_management.entity.Music;
import com.example.Music_management.exception.AudioNotFoundException;
import com.example.Music_management.exception.InvalidAudioFileException;
import com.example.Music_management.exception.MusicAlreadyExistException;
import com.example.Music_management.exception.MusicNotExistException;
import com.example.Music_management.mapper.MusicMapper;
import com.example.Music_management.service.MusicAudioService;
import com.example.Music_management.service.MusicService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
public class MusicAudioServiceImpl implements MusicAudioService {

    private static final Logger logger = LoggerFactory.getLogger(MusicAudioServiceImpl.class);
    /** 单个音频上限(字节),与 spring.servlet.multipart.max-file-size 协同 */
    private static final long MAX_AUDIO_BYTES = 20L * 1024 * 1024;

    @Autowired
    @Qualifier("minioClient")
    private MinioClient minioClient;

    /** 预签名 URL 给浏览器用,主机名须公网可达,与内网读写客户端分开(见 MinioConfig) */
    @Autowired
    @Qualifier("minioPresignClient")
    private MinioClient minioPresignClient;
    @Autowired
    private MusicService musicService;
    @Autowired
    private MusicMapper musicMapper;

    @Value("${minio.bucket}")
    private String bucket;

    @Override
    public String getAudioUrl(int musicId) {
        Music music = musicService.queryById(musicId);
        if (music == null || music.getTitle() == null) {
            throw new MusicNotExistException("the music you are trying to play is not exist!");
        }
        String key = music.getTitle() + ".mp3";
        try {
            // 先确认对象存在(老种子歌曲多数没有音频),再签发 1 小时有效的 GET URL
            minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(key).build());
            return minioPresignClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(key)
                            .expiry(1, TimeUnit.HOURS)
                            .build());
        } catch (ErrorResponseException e) {
            // NoSuchKey 等:该歌曲没有上传音频
            throw new AudioNotFoundException(
                    String.format("no audio in MinIO for music '%s' (bucket=%s, key=%s)", music.getTitle(), bucket, key));
        } catch (Exception e) {
            throw new RuntimeException("failed to access MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public int uploadMusic(MusicCmd cmd, MultipartFile file) {
        // ---- 1) 元数据与文件校验 ----
        String title = cmd == null || cmd.getTitle() == null ? "" : cmd.getTitle().trim();
        String artist = cmd == null || cmd.getArtist() == null ? "" : cmd.getArtist().trim();
        if (title.isEmpty() || artist.isEmpty()) {
            throw new InvalidAudioFileException("title and artist are required");
        }
        if (title.contains("/")) {
            // title 决定 MinIO key(<title>.mp3),斜杠会破坏对象 key
            throw new InvalidAudioFileException("title cannot contain '/'");
        }
        if (file == null || file.isEmpty()) {
            throw new InvalidAudioFileException("audio file is required (.mp3)");
        }
        if (file.getSize() > MAX_AUDIO_BYTES) {
            throw new InvalidAudioFileException("audio file is too large: at most 20MB, but got "
                    + (file.getSize() / (1024 * 1024)) + "MB");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String contentType = file.getContentType() == null ? "" : file.getContentType();
        if (!original.endsWith(".mp3") && !contentType.contains("mpeg")) {
            throw new InvalidAudioFileException("only .mp3 audio is supported, but got: " + contentType);
        }
        if (musicMapper.queryByTitle(title) != null) {
            throw new MusicAlreadyExistException("music title '" + title + "' already exists, please rename");
        }
        cmd.setTitle(title);
        cmd.setArtist(artist);

        // ---- 2) 音频进 MinIO(key = <title>.mp3) ----
        String key = title + ".mp3";
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(in, file.getSize(), -1)
                    .contentType("audio/mpeg")
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("failed to upload audio to MinIO: " + e.getMessage(), e);
        }

        // ---- 3) 复用现有 addMusic:落库(status=ONLINE) + 发 Kafka(消费端进榜/发通知邮件) ----
        try {
            musicService.addMusic(cmd);
            Music saved = musicMapper.queryByTitle(title);
            if (saved == null) {
                throw new IllegalStateException("music inserted but not found by title: " + title);
            }
            logger.info("event=MUSIC_UPLOADED id={} title='{}' tags='{}' size={}B key='{}'",
                    saved.getId(), title, cmd.getTags(), file.getSize(), key);
            return saved.getId();
        } catch (Exception e) {
            // 落库失败 → 删除刚上传的对象,不留孤儿音频
            try {
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());
                logger.warn("event=MUSIC_UPLOAD_COMPENSATED removed orphan object '{}' after DB failure", key);
            } catch (Exception re) {
                logger.error("event=MUSIC_UPLOAD_COMPENSATE_FAILED key='{}' error={}", key, re.toString());
            }
            throw new RuntimeException("failed to save music record: " + e.getMessage(), e);
        }
    }
}
