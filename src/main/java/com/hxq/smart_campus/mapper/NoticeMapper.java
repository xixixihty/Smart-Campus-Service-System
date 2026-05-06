package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.NoticeCreateDTO;
import com.hxq.smart_campus.entity.dto.NoticeUpdateDTO;
import com.hxq.smart_campus.entity.vo.MyNoticeVO;
import com.hxq.smart_campus.entity.vo.NoticeDetailVO;
import com.hxq.smart_campus.entity.vo.NoticeListVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NoticeMapper {

    int insertNotice(NoticeCreateDTO noticeCreateDTO);

    NoticeDetailVO getNoticeDetail(Long id);

    int updateNotice(NoticeUpdateDTO noticeUpdateDTO);

    @Update("UPDATE notice SET status = '撤回', update_time = now() WHERE id = #{id}")
    int withdrawNotice(Long id);

    @Delete("DELETE FROM notice WHERE id = #{id}")
    int deleteNotice(Long id);

    List<NoticeListVO> getNoticeList(@Param("title") String title,
                                     @Param("targetType") String targetType,
                                     @Param("status") String status);

    List<MyNoticeVO> getMyNoticeList(@Param("className") String className,
                                      @Param("majorName") String majorName,
                                      @Param("collegeName") String collegeName);
}
