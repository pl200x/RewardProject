package com.example.Music_management.controller.converter;

import com.example.Music_management.controller.vo.MusicDetailVO;
import com.example.Music_management.entity.Music;

import java.util.ArrayList;
import java.util.List;

public class MusicDetailVOConverter {

    public static MusicDetailVO convertToVO(Music music,double score,long rank){
        MusicDetailVO musicDetailVO = new MusicDetailVO();
        musicDetailVO.setId(music.getId());
        musicDetailVO.setTitle(music.getTitle());
        musicDetailVO.setArtist(music.getArtist());
        musicDetailVO.setReleaseYear(music.getReleaseYear());
        musicDetailVO.setLyrics(music.getLyrics());
        musicDetailVO.setTags(music.getTags());
        musicDetailVO.setStatus(music.getStatus());
        musicDetailVO.setScore(score);
        musicDetailVO.setRanking(rank);
        return musicDetailVO;
    }
    public static List<MusicDetailVO> convertToVOList(List<Music> musicList,List<Double> musicScoreList,long startRank) {
        List<MusicDetailVO> musicVOList = new ArrayList<>();
        for (int i = 0;i < musicList.size();i++) {
            MusicDetailVO musicVO = convertToVO(musicList.get(i),musicScoreList.get(i),startRank+i);
            musicVOList.add(musicVO);
        }
        return musicVOList;
    }
}
