package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.ReadingReportCreateDTO;
import com.hxq.smart_campus.entity.dto.ReadingReportResponseDTO;
import com.hxq.smart_campus.entity.vo.ReadingReportDetailVO;
import com.hxq.smart_campus.entity.vo.ReadingReportListVO;

public interface ReadingReportService {
    /**
     * 获取读书报告列表
     * @param pageNum
     * @param pageSize
     * @param userId
     * @param semester
     * @return
     */
    PageInfo<ReadingReportListVO> getReadingReportList(Integer pageNum, Integer pageSize, Long userId, String semester);
    /**
     * 创建读书报告
     * @param readingReportCreateDTO
     * @return
     */
    ReadingReportResponseDTO insertReadingReport(ReadingReportCreateDTO readingReportCreateDTO);
    /**
     * 获取我的读书报告列表（分页）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param semester 学期（可选）
     * @return 分页列表
     */
    PageInfo<ReadingReportListVO> getMyReadingReportList(Integer pageNum, Integer pageSize, String semester);

    /**
     * 根据ID获取阅读报告详情
     * @param id 报告ID
     * @param currentUserId 当前用户ID（用于归属校验）
     * @return 报告详情
     */
    ReadingReportDetailVO getReadingReportDetailById(Long id, Long currentUserId);
}
