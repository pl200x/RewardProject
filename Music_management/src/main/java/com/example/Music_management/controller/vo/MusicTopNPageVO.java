package com.example.Music_management.controller.vo;

import java.util.List;

public class MusicTopNPageVO {
    private BaseVO baseVO;
    private List<MusicDetailVO> musicList;

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }

    public List<MusicDetailVO> getMusicList() {
        return musicList;
    }

    public void setMusicList(List<MusicDetailVO> musicList) {
        this.musicList = musicList;
    }

    @Override
    public String toString() {
        return "MusicTopNPageVO{" +
                "baseVO=" + baseVO +
                ", musicList=" + musicList +
                '}';
    }

    public MusicTopNPageVO(BaseVO baseVO, List<MusicDetailVO> musicList) {
        this.baseVO = baseVO;
        this.musicList = musicList;
    }

    public MusicTopNPageVO() {
    }
}
