package com.example.Music_management.service.consumer;

import com.example.Music_management.entity.Music;
import com.example.Music_management.exception.MusicNotExistException;
import com.example.Music_management.mapper.MusicMapper;
import com.example.Music_management.repository.MusicRankRepository;
import com.example.Music_management.service.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.Music_management.constant.Constant.MUSIC_TOPIC;

@Service
public class AddMusicConsumer {
    @Autowired
    private MusicService musicService;
    @Autowired
    private MusicMapper musicMapper;
    @Autowired
    private MusicRankRepository musicRankRepository;
    @KafkaListener(topics = MUSIC_TOPIC, groupId = "my-group")
    @Transactional
    public void listen(String content){
        Music selectedMusic =  musicMapper.queryByTitle(content);
        if (selectedMusic == null) {
            // 消息里的歌名查不到(脏消息),重试不可能自愈,直接进死信(见 KafkaErrorConfig)
            throw new MusicNotExistException("music not exist by title: " + content);
        }
        //promote to interested user(tags 为空时 recommend 内部跳过邮件,进榜不受影响)
        musicService.recommend(selectedMusic.getId(), content, selectedMusic.getArtist(), selectedMusic.getTags());
        musicRankRepository.addMusicIntoRank(selectedMusic.getId());
        // 新歌同时进入其所属各 genre 分榜(初始分 0),供"按兴趣推荐"读取
        if (selectedMusic.getTags() != null) {
            for (String genre : selectedMusic.getTags().split(",")) {
                musicRankRepository.addToGenreRank(selectedMusic.getId(), genre, 0);
            }
        }
    }

}
