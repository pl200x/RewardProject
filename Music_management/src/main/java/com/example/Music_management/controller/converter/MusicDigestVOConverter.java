package com.example.Music_management.controller.converter;

import com.example.Music_management.controller.vo.MusicDigestVO;
import com.example.Music_management.entity.Music;

import java.util.ArrayList;
import java.util.List;

public class MusicDigestVOConverter {
    public static MusicDigestVO convertToDigestVO(Music music){
        MusicDigestVO musicDigestVO = new MusicDigestVO();
        musicDigestVO.setArtist(music.getArtist());
        musicDigestVO.setTitle(music.getTitle());
        musicDigestVO.setId(music.getId());
        musicDigestVO.setTags(music.getTags());
        return musicDigestVO;
    }
    public static List<MusicDigestVO> convertToDigestVOList(List<Music> musicList){
        List<MusicDigestVO> musicDigestVOList = new ArrayList<>();
        for(Music music : musicList){
            musicDigestVOList.add(convertToDigestVO(music));
        }
        return musicDigestVOList;
    }
    /** 带人气分(来自 Redis 排行,scores 与 musicList 下标对齐,允许 null/长度不齐) */
    public static List<MusicDigestVO> convertToDigestVOList(List<Music> musicList, List<Double> scores){
        List<MusicDigestVO> voList = new ArrayList<>();
        for (int i = 0; i < musicList.size(); i++) {
            MusicDigestVO vo = convertToDigestVO(musicList.get(i));
            Double s = (scores != null && i < scores.size()) ? scores.get(i) : null;
            vo.setScore(s == null ? 0 : s);
            voList.add(vo);
        }
        return voList;
    }
}
