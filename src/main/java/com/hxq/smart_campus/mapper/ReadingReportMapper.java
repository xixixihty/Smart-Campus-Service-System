package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.ReadingReportCreateDTO;
import com.hxq.smart_campus.entity.pojo.ReadingReport;
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
     * 插入阅读报告（简易版，只含userId和semester）
     * @param readingReportCreateDTO
     * @return
     */
    int insertReadingReport(ReadingReportCreateDTO readingReportCreateDTO);

    /**
     * 插入完整阅读报告（含totalBorrow、favCategory、analysisText）
     * @param report 阅读报告实体
     * @return
     */
    int insertFullReport(ReadingReport report);

    /**
     * 获取最新插入的ID
     * @return
     */
    @Select("select last_insert_id()")
    Long getLastInsertId();

    /**
     * 根据用户ID和学期获取最新报告
     * @param userId 用户ID
     * @param semester 学期（可选）
     * @return 报告详情
     */
    ReadingReportDetailVO getReadingReportByUser(@Param("userId") Long userId,
                                                  @Param("semester") String semester);

    /**
     * 根据ID获取阅读报告详情
     * @param id
     * @return
     */
    ReadingReportDetailVO getReadingReportDetailById(Long id);

    /**
     * 更新阅读报告的分析数据
     * @param id 报告ID
     * @param totalBorrow 总借阅量
     * @param favCategory 偏好分类
     * @param analysisText 分析文本
     * @return
     */
    int updateReportAnalysis(@Param("id") Long id,
                             @Param("totalBorrow") Integer totalBorrow,
                             @Param("favCategory") String favCategory,
                             @Param("analysisText") String analysisText);
}