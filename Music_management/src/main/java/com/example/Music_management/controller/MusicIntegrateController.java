package com.example.Music_management.controller;

import com.example.Music_management.controller.cmd.PlayMusicCmd;
import com.example.Music_management.controller.vo.BaseVO;
import com.example.Music_management.exception.UserNotExistException;
import com.example.Music_management.integration.PlayRecordCmd;
import com.example.Music_management.security.JwtAuthFilter;
import com.example.Music_management.service.PlayMusicService;
import com.example.Music_management.service.impl.NotificationServiceImpl;
import com.example.Music_management.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;

@RestController
@RequestMapping("/music")
public class MusicIntegrateController {

    @Autowired
    private PlayMusicService playMusicService;
    private static final Logger logger = LoggerFactory.getLogger(MusicIntegrateController.class);
    @PostMapping("/play")
    public BaseVO play(@RequestBody PlayMusicCmd playMusicCmd, HttpServletRequest request){
        long start = System.currentTimeMillis();
        long end;
        try{
            Integer authenticatedUserId =
                    (Integer) request.getAttribute(JwtAuthFilter.AUTHENTICATED_USER_ID);
            if (authenticatedUserId == null || authenticatedUserId != playMusicCmd.getUserId()) {
                return BaseVO.buildVO(403, System.currentTimeMillis() - start, false,
                        "cannot submit play records for another user");
            }
            playMusicService.sync(playMusicCmd.getUserId(), playMusicCmd.getMusicId(),
                    DateTimeUtil.parse(playMusicCmd.getSyncTime(),"yyyy-MM-dd HH:mm:ss"),
                    playMusicCmd.getDuration(), playMusicCmd.getScene(), playMusicCmd.getCode());
            return BaseVO.buildVO(200,System.currentTimeMillis()-start,true,null);
        }catch(Exception e){
            logger.error(e.toString());
            return BaseVO.buildVO(500,System.currentTimeMillis()-start,false,e.getMessage());
        }
    }


}
