package com.example.Music_management.controller;

import com.example.Music_management.controller.converter.MusicDigestVOConverter;
import com.example.Music_management.controller.vo.BaseVO;
import com.example.Music_management.controller.vo.RecommendMusicPageVO;
import com.example.Music_management.entity.Music;
import com.example.Music_management.repository.MusicRankRepository;
import com.example.Music_management.service.MusicRankService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/rank")
public class MusicRankController {
    private static final Logger logger = LoggerFactory.getLogger(MusicRankController.class);

    @Autowired
    private MusicRankService musicRankService;
    @Autowired
    private MusicRankRepository musicRankRepository;

    private List<Integer> musicIdsConverter(List<Music> musicList){
        List<Integer> musicRankListIds = new ArrayList<>();
        for(Music music: musicList){
            musicRankListIds.add(music.getId());
        }
        return musicRankListIds;
    }

    /** 把榜单实体列表组装成 digest 响应(score 来自 Redis,排名由前端按序号展示;详情走 /music/id)。 */
    private RecommendMusicPageVO buildDigestPage(List<Music> musicList, long cost) {
        RecommendMusicPageVO result = new RecommendMusicPageVO();
        List<Integer> ids = musicIdsConverter(musicList);
        List<Double> scores = ids.isEmpty() ? new ArrayList<>() : musicRankRepository.getScoreByMusicIds(ids);
        result.setMusicDigestVOList(MusicDigestVOConverter.convertToDigestVOList(musicList, scores));
        result.setBaseVO(BaseVO.buildVO(200, cost, true, null));
        return result;
    }

    /** 全局热度榜(digest) —— 首页中列,条数由用户设定(默认 Top 10)。 */
    @GetMapping("/top")
    public RecommendMusicPageVO getTopN(Integer N) {
        long start = System.currentTimeMillis();
        try {
            // N 缺省/非法时回落 10,避免 N=0 时 Redis range(0,-1) 拉回整个榜单
            int n = (N == null || N <= 0) ? 10 : N;
            List<Music> musicList = musicRankService.getTopN(n);
            return buildDigestPage(musicList, System.currentTimeMillis() - start);
        } catch (Exception e) {
            logger.error(e.toString());
            RecommendMusicPageVO result = new RecommendMusicPageVO();
            result.setBaseVO(BaseVO.buildVO(500, System.currentTimeMillis() - start, false, "other unknown error"));
            return result;
        }
    }

    /** 按兴趣个性化推荐(digest) —— 首页右列。userId 为空(游客)时回退全局 TopN。 */
    @GetMapping("/recommend")
    public RecommendMusicPageVO recommend(Integer userId, Integer N) {
        long start = System.currentTimeMillis();
        try {
            int n = (N == null || N <= 0) ? 10 : N;
            List<Music> musicList = (userId == null)
                    ? musicRankService.getTopN(n)
                    : musicRankService.getTopNByInterest(userId, n);
            return buildDigestPage(musicList, System.currentTimeMillis() - start);
        } catch (Exception e) {
            logger.error(e.toString());
            RecommendMusicPageVO result = new RecommendMusicPageVO();
            result.setBaseVO(BaseVO.buildVO(500, System.currentTimeMillis() - start, false, "other unknown error"));
            return result;
        }
    }
}
