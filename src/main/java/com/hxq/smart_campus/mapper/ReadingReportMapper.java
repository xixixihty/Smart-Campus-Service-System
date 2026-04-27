package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.ReadingReportCreateDTO;
import com.hxq.smart_campus.entity.vo.ReadingReportDetailVO;
import com.hxq.smart_campus.entity.vo.ReadingReportListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReadingReportMapper {
    /**
     * 获取阅读报告列表
     * @param userId
     * @param semester
     * @return
     */
    List<ReadingReportListVO> getReadingReportList(@Param("userId") Long userId,
                                                   @Param("semester") String semester);
    /**
     * 插入阅读报告
     * @param readingReportCreateDTO
     * @return
     */
    int insertReadingReport(ReadingReportCreateDTO readingReportCreateDTO);
    /**
     * 获取最新插入的ID
     * @return
     */
    @Select("select last_insert_id()")
    Long getLastInsertId();
    /**
     * 根据ID获取阅读报告详情
     * @param id
     * @return
     */
    ReadingReportDetailVO getReadingReportDetailById(Long id);
}
