package com.example.Music_management.controller.vo;

/**
 * 登录返回:token + 用户身份 + 通用 baseVO。
 * 前端拿到 token 后存储,并在后续请求头带 Authorization: Bearer <token>;
 * userId 也由此返回,前端无需手填。
 * interest:用户当前的兴趣标签(逗号分隔字符串);前端据此判断是否首登(空=首登→引导选兴趣)、
 *          以及编辑兴趣时回填。
 */
public class LoginVO {
    private String token;
    private Integer userId;
    private String userName;
    private String interest;
    /** 当前头像序号 0-9,前端据此渲染右上角头像并回填选择器 */
    private Integer avatar;
    private BaseVO baseVO;

    public LoginVO() {
    }

    public static LoginVO success(String token, Integer userId, String userName, String interest, Integer avatar, long time) {
        LoginVO vo = new LoginVO();
        vo.token = token;
        vo.userId = userId;
        vo.userName = userName;
        vo.interest = interest;
        vo.avatar = avatar;
        vo.baseVO = BaseVO.buildVO(200, time, true, null);
        return vo;
    }

    public static LoginVO fail(int code, long time, String errorMessage) {
        LoginVO vo = new LoginVO();
        vo.baseVO = BaseVO.buildVO(code, time, false, errorMessage);
        return vo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public Integer getAvatar() {
        return avatar;
    }

    public void setAvatar(Integer avatar) {
        this.avatar = avatar;
    }

    public BaseVO getBaseVO() {
        return baseVO;
    }

    public void setBaseVO(BaseVO baseVO) {
        this.baseVO = baseVO;
    }
}
