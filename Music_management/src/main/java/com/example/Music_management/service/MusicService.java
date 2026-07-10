package com.example.Music_management.service;

import com.example.Music_management.controller.cmd.MusicCmd;
import com.example.Music_management.entity.Music;

import java.util.List;

public interface MusicService {
   Music queryById(int id);
   List<Music> queryAll();
   /** 最近新增的 n 首(按 id 倒序,id 近似插入顺序) —— 首页 "Recent Music" 列用 */
   List<Music> getRecent(int n);
   /** 按名称(标题/艺人)模糊搜索,返回上架的匹配列表 */
   List<Music> queryByName(String name);
   void addMusic(MusicCmd musicCmd);
   /** 新歌上新通知(Kafka 消费端调用):按 tags 给兴趣用户发个性化邮件,附详情页直达链接;tags 为空则跳过 */
   void recommend(int musicId, String title, String artist, String musicTags);
   void deleteMusic(int musicId);


}
