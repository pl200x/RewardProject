package com.example.Multi_reward.controller.vo;

public class BaseVO {
    private boolean success;
    private int code;
    private long duration;
    private String errorMessage;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "BaseVO{" +
                "success=" + success +
                ", code=" + code +
                ", duration=" + duration +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

    public BaseVO(boolean success, int code, long duration, String errorMessage) {
        this.success = success;
        this.code = code;
        this.duration = duration;
        this.errorMessage = errorMessage;
    }

    public BaseVO() {
    }
    public static BaseVO buildBaseVO(boolean success, int code, long duration, String errorMessage){
        BaseVO baseVO = new BaseVO();
        baseVO.setSuccess(success);
        baseVO.setCode(code);
        baseVO.setDuration(duration);
        baseVO.setErrorMessage(errorMessage);
        return baseVO;
    }
}
