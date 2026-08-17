package com.example.ai_chatbot.integration.VO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MultiPlayRecordPageVO {
    private List<PlayRecordVO> playRecordVOList;
    private BaseVO baseVO;

    public List<PlayRecordVO> getPlayRecordVOList() {
        return playRecordVOList;
    }

    public void setPlayRecordVOList(List<PlayRecordVO> playRecordVOList) {
        this.playRecordVOList = playRecordVOList;
    }

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }

    public MultiPlayRecordPageVO() {
    }

    public MultiPlayRecordPageVO(List<PlayRecordVO> playRecordVOList, BaseVO baseVO) {
        this.playRecordVOList = playRecordVOList;
        this.baseVO = baseVO;
    }

    @Override
    public String toString() {
        return "MultiPlayRecordPageVO{" +
                "playRecordVOList=" + playRecordVOList +
                ", baseVO=" + baseVO +
                '}';
    }

}