package com.example.Music_management.service.impl;

import com.example.Music_management.entity.Music;
import com.example.Music_management.entity.User;
import com.example.Music_management.mapper.MusicMapper;
import com.example.Music_management.repository.MusicRankRepository;
import com.example.Music_management.service.MusicRankService;
import com.example.Music_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MusicRankServiceImpl implements MusicRankService {
    @Autowired
    private MusicRankRepository musicRankRepository;

    @Autowired
    private MusicMapper musicMapper;

    @Autowired
    private UserService userService;

    @Override
    public List<Music> getTopN(int n) {
        Set<ZSetOperations.TypedTuple<Integer>> topN = musicRankRepository.getTopN(n - 1);
        List<Integer> resultIds = new ArrayList<>();
        if (topN != null) {
            for (ZSetOperations.TypedTuple<Integer> item : topN) {
                if (item.getValue() != null) resultIds.add(item.getValue());
            }
        }
        // 排行榜 ZSet 为空时直接返回空列表,避免 queryByIds 拼出 "ORDER BY FIELD(id,)" 报错
        if (resultIds.isEmpty()) return new ArrayList<>();
        // 榜单为列表场景:digest 列裁剪,不序列化 lyrics
        return musicMapper.queryDigestByIds(resultIds);
    }

    @Override
    public List<Music> getTopNByInterest(int userId, int n) {
        User user = userService.queryById(userId);
        if (user == null || user.getInterest() == null || user.getInterest().trim().isEmpty()) {
            return getTopN(n); // 无兴趣 → 回退全局
        }
        // 逐个 genre 读分榜,合并到一张表:同一首歌在多个兴趣里出现时取较高分
        Map<Integer, Double> merged = new LinkedHashMap<>();
        for (String genre : user.getInterest().split(",")) {
            if (genre == null || genre.trim().isEmpty()) continue;
            Set<ZSetOperations.TypedTuple<Integer>> top = musicRankRepository.getTopNByGenre(genre, n);
            if (top == null) continue;
            for (ZSetOperations.TypedTuple<Integer> t : top) {
                Integer id = t.getValue();
                if (id == null) continue;
                double sc = t.getScore() == null ? 0 : t.getScore();
                merged.merge(id, sc, Math::max);
            }
        }
        if (merged.isEmpty()) {
            return getTopN(n); // 兴趣对应的分榜都没数据 → 回退全局
        }
        List<Integer> ids = merged.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(n)
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
        return musicMapper.queryDigestByIds(ids);
    }
}
