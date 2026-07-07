package com.example.Music_management.mapper;

import com.example.Music_management.entity.Music;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MusicMapper {
    void addMusic(Music music);
    void updateMusicStatus(int id, String status);
    void updateTag(int id, String tags);
    Music queryById(int id);

    List<Music> queryAll();
    List<Music> queryByIds( List<Integer> ids);
    List<Music> queryRecent(int n);
    List<Music> queryByName(String name);
    Music queryByTitle(String title);

    // ==== 列表用 digest 查询:只取 id/title/artist/tags,不碰 lyrics ====
    // queryRecentDigest 走覆盖索引 idx_music_online_digest(status,id,title,artist,tags),不回表
    List<Music> queryRecentDigest(int n);
    List<Music> queryDigestByIds(List<Integer> ids);
    List<Music> queryByNameDigest(String name);

}
