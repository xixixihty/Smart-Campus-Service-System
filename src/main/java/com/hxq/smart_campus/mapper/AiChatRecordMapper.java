package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.AiChatHistoryDTO;
import com.hxq.smart_campus.entity.dto.AiSessionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiChatRecordMapper {

    int insertRecord(@Param("userId") Long userId, @Param("sessionId") String sessionId,
                     @Param("role") String role, @Param("content") String content);

    int upsertSession(@Param("userId") Long userId, @Param("sessionId") String sessionId,
                      @Param("title") String title);

    int updateSessionMessageCount(@Param("sessionId") String sessionId, @Param("count") int count);

    List<AiSessionDTO> listSessionsByUserId(@Param("userId") Long userId);

    List<AiChatHistoryDTO> listRecordsBySessionId(@Param("sessionId") String sessionId,
                                                   @Param("limit") int limit);

    int deleteSession(@Param("sessionId") String sessionId, @Param("userId") Long userId);

    int deleteRecordsBySessionId(@Param("sessionId") String sessionId);
}