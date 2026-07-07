package com.example.Music_management.controller.vo;

import java.util.List;

public class RecommendMusicPageVO {
    private List<MusicDigestVO> musicDigestVOList;
    private BaseVO baseVO;

    public List<MusicDigestVO> getMusicDigestVOList() {
        return musicDigestVOList;
    }

    public void setMusicDigestVOList(List<MusicDigestVO> musicDigestVOList) {
        this.musicDigestVOList = musicDigestVOList;
    }

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }

    @Override
    public String toString() {
        return "RecommendMusicPageVO{" +
                "musicDigestVOList=" + musicDigestVOList +
                ", baseVO=" + baseVO +
                '}';
    }

    public RecommendMusicPageVO() {
    }

    public RecommendMusicPageVO(List<MusicDigestVO> musicDigestVOList, BaseVO baseVO) {
        this.musicDigestVOList = musicDigestVOList;
        this.baseVO = baseVO;
    }
}
