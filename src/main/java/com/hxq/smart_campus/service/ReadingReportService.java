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
     * 获取我的读书报告详情
     * @param semester
     * @return
     */
    ReadingReportDetailVO getMyReadingReportDetail(String semester);
}
