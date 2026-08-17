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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;

@Service
public class MusicAudioServiceImpl implements MusicAudioService {

    private static final Logger logger = LoggerFactory.getLogger(MusicAudioServiceImpl.class);
    /** 单个音频上限(字节),与 spring.servlet.multipart.max-file-size 协同 */
    private static final long MAX_AUDIO_BYTES = 20L * 1024 * 1024;

    @Autowired
    private S3Client s3Client;
    @Autowired
    private S3Presigner s3Presigner;
    @Autowired
    private MusicService musicService;
    @Autowired
    private MusicMapper musicMapper;

    @Value("${storage.s3.bucket}")
    private String bucket;

    @Value("${storage.s3.prefix:audio}")
    private String prefix;

    @Override
    public String getAudioUrl(int musicId) {
        Music music = musicService.queryById(musicId);
        if (music == null || music.getTitle() == null) {
            throw new MusicNotExistException("the music you are trying to play is not exist!");
        }
        String key = objectKey(music.getTitle());
        try {
            // 老种子歌曲多数没有音频:先 HEAD 确认对象存在,再签发 1 小时 GET URL。
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .responseContentType("audio/mpeg")
                    .build();
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1))
                    .getObjectRequest(getObjectRequest)
                    .build();
            return s3Presigner.presignGetObject(presignRequest).url().toExternalForm();
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new AudioNotFoundException(
                        String.format("no audio in S3 for music '%s' (bucket=%s, key=%s)",
                                music.getTitle(), bucket, key));
            }
            throw new RuntimeException("failed to access S3: " + safeS3Message(e), e);
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
            // title 决定 S3 key,斜杠会意外创建额外路径层级
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

        // ---- 2) 音频进私有 S3 bucket ----
        String key = objectKey(title);
        try (InputStream in = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("audio/mpeg")
                    .contentLength(file.getSize())
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(in, file.getSize()));
        } catch (Exception e) {
            throw new RuntimeException("failed to upload audio to S3: " + e.getMessage(), e);
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
                s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
                logger.warn("event=MUSIC_UPLOAD_COMPENSATED removed orphan object '{}' after DB failure", key);
            } catch (Exception re) {
                logger.error("event=MUSIC_UPLOAD_COMPENSATE_FAILED key='{}' error={}", key, re.toString());
            }
            throw new RuntimeException("failed to save music record: " + e.getMessage(), e);
        }
    }

    private String objectKey(String title) {
        String normalizedPrefix = StringUtils.hasText(prefix)
                ? prefix.replaceAll("^/+|/+$", "") + "/"
                : "";
        return normalizedPrefix + title + ".mp3";
    }

    private String safeS3Message(S3Exception exception) {
        if (exception.awsErrorDetails() != null && exception.awsErrorDetails().errorCode() != null) {
            return exception.awsErrorDetails().errorCode();
        }
        return "HTTP " + exception.statusCode();
    }
}
