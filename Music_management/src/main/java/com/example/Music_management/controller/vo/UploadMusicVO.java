package com.example.Music_management.controller.vo;

/** 上传音乐返回:新歌 id(前端据此直接跳详情页试听) + 通用 baseVO。 */
public class UploadMusicVO {
    private Integer musicId;
    private BaseVO baseVO;

    public UploadMusicVO() {
    }

    public Integer getMusicId() {
        return musicId;
    }

    public void setMusicId(Integer musicId) {
        this.musicId = musicId;
    }

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }
}
