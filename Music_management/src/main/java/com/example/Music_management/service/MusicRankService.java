package com.example.Music_management.service;
import com.example.Music_management.entity.Music;

import java.util.List;

public interface MusicRankService {
    List<Music> getTopN(int n);

    /**
     * 按用户兴趣做个性化推荐 TopN:读用户 interest 里每个 genre 的分榜,合并取前 n。
     * 无兴趣 / 分榜无数据时回退到全局 TopN。
     */
    List<Music> getTopNByInterest(int userId, int n);
}
