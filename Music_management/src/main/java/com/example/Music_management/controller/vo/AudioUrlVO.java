package com.example.Music_management.controller.vo;

/** 歌曲音频播放地址(MinIO presigned URL,1 小时有效)。 */
public class AudioUrlVO {
    private String url;
    private BaseVO baseVO;

    public AudioUrlVO() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }
}
