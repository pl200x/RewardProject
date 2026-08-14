package com.example.ai_chatbot.mapper;

import com.example.ai_chatbot.entity.ChatContext;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatContextMapper {

    int insert(ChatContext chatContext);

    ChatContext findById(@Param("id") int id, @Param("userId") int userId);

    List<ChatContext> findBySessionId(
            @Param("sessionId") String sessionId,
            @Param("userId") int userId);

    int updateReadStatus(
            @Param("id") int id,
            @Param("userId") int userId,
            @Param("readStatus") boolean readStatus);

    int updateSessionReadStatus(
            @Param("sessionId") String sessionId,
            @Param("userId") int userId,
            @Param("readStatus") boolean readStatus);

    int deleteById(@Param("id") int id, @Param("userId") int userId);

    int deleteBySessionId(
            @Param("sessionId") String sessionId,
            @Param("userId") int userId);
}
