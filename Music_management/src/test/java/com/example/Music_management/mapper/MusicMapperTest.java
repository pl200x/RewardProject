package com.example.Music_management.mapper;

import com.example.Music_management.entity.Music;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.Arrays;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MusicMapperTest {
    @Autowired
    private MusicMapper musicMapper;

    @Test
    public void testAddMusic(){
        Music music = new Music();
        music.setArtist("artist2");
        music.setTitle("t2");
        music.setReleaseYear(2013);
        music.setTags("tag2");
        music.setStatus("online");
        musicMapper.addMusic(music);
    }
    @Test
    public void testGetMusic(){
        Music music = musicMapper.queryById(1);
        System.out.println("you get music : "+ music);
        Assert.assertEquals("title1",music.getTitle());
        Assert.assertEquals(2023,music.getReleaseYear());
        Assert.assertNull(music.getLyrics());
    }
    @Test
    public void testGetAllMusic(){
//        List<Integer> ids = new ArrayList<>();
//        ids.add(1);
//        ids.add(2);
        List<Integer> ids = Arrays.asList(1,2);
        List<Music> musics = musicMapper.queryByIds(ids);
        System.out.println(musics);
    }
}
