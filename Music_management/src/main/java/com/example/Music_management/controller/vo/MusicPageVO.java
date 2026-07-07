package com.example.Music_management.controller.vo;

public class MusicPageVO {
    private BaseVO baseVO;
    private MusicDetailVO musicDetailVO;

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }

    public MusicDetailVO getMusicDetailVO() {
        return musicDetailVO;
    }

    public void setMusicDetailVO(MusicDetailVO musicDetailVO) {
        this.musicDetailVO = musicDetailVO;
    }

    public MusicPageVO(BaseVO baseVO, MusicDetailVO musicDetailVO) {
        this.baseVO = baseVO;
        this.musicDetailVO = musicDetailVO;
    }

    public MusicPageVO() {
    }

    @Override
    public String toString() {
        return "MusicPageVO{" +
                "baseVO=" + baseVO +
                ", musicDetailVO=" + musicDetailVO +
                '}';
    }
}
