package com.example.Music_management.service.impl;

import com.example.Music_management.entity.Music;
import com.example.Music_management.entity.User;
import com.example.Music_management.exception.MusicNotExistException;
import com.example.Music_management.exception.UserNotExistException;
import com.example.Music_management.integration.MusicIntegration;
import com.example.Music_management.integration.PlayRecordCmd;
import com.example.Music_management.service.MusicService;
import com.example.Music_management.service.PlayMusicService;
import com.example.Music_management.service.UserService;
import com.example.Music_management.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PlayMusicServiceImpl implements PlayMusicService {
    @Autowired
    private MusicIntegration musicIntegration;
    @Autowired
    private UserService userService;
    @Autowired
    private MusicService musicService;
    @Override
    public void sync(int userId, int musicId, Date syncTime, int duration, String scene, String code) {
        User user = userService.queryById(userId);
        Music music = musicService.queryById(musicId);
        if(user == null){
            throw new UserNotExistException("the user you are trying to sync is not exist");
        }
        if (music == null) {
            throw new MusicNotExistException("the music you are trying to sync is not exist!");
        }
        String dateTime = DateTimeUtil.parstDateToString(syncTime,"yyyy-MM-dd HH:mm:ss");

        PlayRecordCmd playRecordCmd = new PlayRecordCmd();
        playRecordCmd.setUserId(userId);
        playRecordCmd.setMusicId(musicId);
        playRecordCmd.setSyncTime(dateTime);
        playRecordCmd.setDuration(duration);
        playRecordCmd.setScene(scene);
        playRecordCmd.setCode(code);
        musicIntegration.syncMusicPlayRecord(playRecordCmd);
    }

}
