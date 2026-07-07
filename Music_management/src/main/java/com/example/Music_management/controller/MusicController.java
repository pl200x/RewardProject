package com.example.Music_management.controller;

import com.example.Music_management.controller.cmd.MusicCmd;
import com.example.Music_management.controller.converter.MusicDetailVOConverter;
import com.example.Music_management.controller.converter.MusicDigestVOConverter;
import com.example.Music_management.controller.vo.AudioUrlVO;
import com.example.Music_management.controller.vo.BaseVO;
import com.example.Music_management.controller.vo.MusicPageVO;
import com.example.Music_management.controller.vo.RecommendMusicPageVO;
import com.example.Music_management.controller.vo.UploadMusicVO;
import com.example.Music_management.entity.Music;
import com.example.Music_management.exception.AudioNotFoundException;
import com.example.Music_management.exception.InvalidAudioFileException;
import com.example.Music_management.exception.MusicAlreadyExistException;
import com.example.Music_management.exception.MusicNotExistException;
import com.example.Music_management.service.MusicAudioService;
import org.springframework.web.multipart.MultipartFile;
import com.example.Music_management.exception.IncorrectPasswordException;
import com.example.Music_management.exception.UserInputException;
import com.example.Music_management.exception.UserNotExistException;
import com.example.Music_management.repository.MusicRankRepository;
import com.example.Music_management.service.MusicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/music")
public class MusicController {
    private static final Logger logger = LoggerFactory.getLogger(MusicController.class);
    @Autowired
    private MusicService musicService;
    @Autowired
    private MusicRankRepository musicRankRepository;
    @Autowired
    private MusicAudioService musicAudioService;
    @PostMapping("/add")
    public BaseVO addMusic(@RequestBody MusicCmd musicCmd){
        long start = System.currentTimeMillis();
        long end;
        try{

            musicService.addMusic(musicCmd);
            end = System.currentTimeMillis();
            return BaseVO.buildVO(200,end - start,true,null);
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return BaseVO.buildVO(500,end - start,false,"other unknown error");
        }
    }

   @GetMapping("/id")
   public MusicPageVO searchMusic(int id){
        long start = System.currentTimeMillis();
        long end;
        MusicPageVO musicPageVO = new MusicPageVO();
        try{
            Music music = musicService.queryById(id);
            double score = musicRankRepository.getScoreByMusicId(id);
            long rank = musicRankRepository.getRankByMusicId(id);
            end = System.currentTimeMillis();
            musicPageVO.setBaseVO(BaseVO.buildVO(200,end - start,true,null));
            musicPageVO.setMusicDetailVO(MusicDetailVOConverter.convertToVO(music,score,rank));
            return musicPageVO;
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            musicPageVO.setBaseVO(BaseVO.buildVO(500,end - start,false,"other unknown error"));
            return musicPageVO;
        }
    }
    @GetMapping("/list")
    public RecommendMusicPageVO recommendMusic(){
        long start = System.currentTimeMillis();
        long end;
        RecommendMusicPageVO recommendMusicPageVO = new RecommendMusicPageVO();
        try{
            List<Music> musicList = musicService.queryAll();
            end = System.currentTimeMillis();
            recommendMusicPageVO.setBaseVO(BaseVO.buildVO(200,end - start,true,null));
            recommendMusicPageVO.setMusicDigestVOList(MusicDigestVOConverter.convertToDigestVOList(musicList));
            return recommendMusicPageVO;
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            recommendMusicPageVO.setBaseVO(BaseVO.buildVO(500,end - start,false,"other unknown error"));
            return recommendMusicPageVO;
        }
    }
    /** 最近新增(默认 10 首,digest) —— 首页左列。走覆盖索引不回表;score 取全局排行分,无则 0。 */
    @GetMapping("/recent")
    public RecommendMusicPageVO recent(Integer n){
        long start = System.currentTimeMillis();
        long end;
        RecommendMusicPageVO result = new RecommendMusicPageVO();
        try{
            int limit = (n == null || n <= 0) ? 10 : n;
            List<Music> musicList = musicService.getRecent(limit);
            List<Integer> ids = new ArrayList<>();
            for (Music m : musicList) ids.add(m.getId());
            List<Double> scores = ids.isEmpty() ? new ArrayList<>() : musicRankRepository.getScoreByMusicIds(ids);
            end = System.currentTimeMillis();
            result.setMusicDigestVOList(MusicDigestVOConverter.convertToDigestVOList(musicList, scores));
            result.setBaseVO(BaseVO.buildVO(200, end - start, true, null));
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            result.setBaseVO(BaseVO.buildVO(500, end - start, false, "other unknown error"));
        }
        return result;
    }

    /** 按名称搜索(标题/艺人模糊匹配,digest)。无命中时 success=true、列表为空,errorMessage 带提示文案。 */
    @GetMapping("/search")
    public RecommendMusicPageVO search(String name){
        long start = System.currentTimeMillis();
        long end;
        RecommendMusicPageVO result = new RecommendMusicPageVO();
        try{
            List<Music> musicList = (name == null || name.trim().isEmpty())
                    ? new ArrayList<>() : musicService.queryByName(name.trim());
            List<Integer> ids = new ArrayList<>();
            for (Music m : musicList) ids.add(m.getId());
            List<Double> scores = ids.isEmpty() ? new ArrayList<>() : musicRankRepository.getScoreByMusicIds(ids);
            end = System.currentTimeMillis();
            result.setMusicDigestVOList(MusicDigestVOConverter.convertToDigestVOList(musicList, scores));
            String msg = musicList.isEmpty() ? "the music you are finding are not found, we will update soon" : null;
            result.setBaseVO(BaseVO.buildVO(200, end - start, true, msg));
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            result.setBaseVO(BaseVO.buildVO(500, end - start, false, "other unknown error"));
        }
        return result;
    }
    /**
     * 上传音乐(登录用户,账户菜单入口):multipart = 元数据(MusicCmd 各字段) + mp3 文件。
     * 音频进 MinIO(key=<title>.mp3),元数据复用 addMusic 落库+Kafka(消费端进榜/按 tags 发通知邮件,tags 为空则跳过邮件)。
     * 成功返回新歌 id,前端直接跳详情页试听。
     */
    @PostMapping("/upload")
    public UploadMusicVO upload(@ModelAttribute MusicCmd musicCmd, @RequestParam("file") MultipartFile file){
        long start = System.currentTimeMillis();
        long end;
        UploadMusicVO vo = new UploadMusicVO();
        try{
            int musicId = musicAudioService.uploadMusic(musicCmd, file);
            end = System.currentTimeMillis();
            vo.setMusicId(musicId);
            vo.setBaseVO(BaseVO.buildVO(200, end - start, true, null));
        }catch(InvalidAudioFileException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            vo.setBaseVO(BaseVO.buildVO(501, end - start, false, e.getMessage()));
        }catch(MusicAlreadyExistException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            vo.setBaseVO(BaseVO.buildVO(502, end - start, false, e.getMessage()));
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            vo.setBaseVO(BaseVO.buildVO(500, end - start, false, "other unknown error"));
        }
        return vo;
    }

    /** 歌曲音频播放地址(MinIO presigned URL,1h)。无音频/无歌曲时透传具体错误。公开接口,游客可播。 */
    @GetMapping("/audio-url")
    public AudioUrlVO audioUrl(int id){
        long start = System.currentTimeMillis();
        long end;
        AudioUrlVO vo = new AudioUrlVO();
        try{
            String url = musicAudioService.getAudioUrl(id);
            end = System.currentTimeMillis();
            vo.setUrl(url);
            vo.setBaseVO(BaseVO.buildVO(200, end - start, true, null));
        }catch(MusicNotExistException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            vo.setBaseVO(BaseVO.buildVO(502, end - start, false, e.getMessage()));
        }catch(AudioNotFoundException e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            vo.setBaseVO(BaseVO.buildVO(504, end - start, false, e.getMessage()));
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            vo.setBaseVO(BaseVO.buildVO(500, end - start, false, "other unknown error"));
        }
        return vo;
    }
    @DeleteMapping("/id")
    public BaseVO deleteMusic(int musicId){
        long start = System.currentTimeMillis();
        long end;
        try{
            musicService.deleteMusic(musicId);
            end = System.currentTimeMillis();
            return BaseVO.buildVO(200,end - start,true,null);
        }catch(Exception e){
            logger.error(e.toString());
            end = System.currentTimeMillis();
            return BaseVO.buildVO(500,end - start,false,"other unknown error");
        }
    }
}
