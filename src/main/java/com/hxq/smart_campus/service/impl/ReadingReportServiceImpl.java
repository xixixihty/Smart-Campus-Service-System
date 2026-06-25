package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.ReadingReportCreateDTO;
import com.hxq.smart_campus.entity.dto.ReadingReportResponseDTO;
import com.hxq.smart_campus.entity.vo.ReadingAnalysisVO;
import com.hxq.smart_campus.entity.vo.ReadingReportDetailVO;
import com.hxq.smart_campus.entity.vo.ReadingReportListVO;
import com.hxq.smart_campus.mapper.ReadingReportMapper;
import com.hxq.smart_campus.service.ReadingReportService;
import com.hxq.smart_campus.service.ai.ReadingAnalysisService;
import com.hxq.smart_campus.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.hxq.smart_campus.constant.MessageConstant.DATE_TIME_FORMATTER;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadingReportServiceImpl implements ReadingReportService {

    private final ReadingReportMapper readingReportMapper;
    private final ReadingAnalysisService readingAnalysisService;

    /**
     * 获取阅读报告列表
     */
    @Override
    public PageInfo<ReadingReportListVO> getReadingReportList(Integer pageNum, Integer pageSize, Long userId, String semester) {
        PageHelper.startPage(pageNum, pageSize);
        List<ReadingReportListVO> readingReportListVOS = readingReportMapper.getReadingReportList(userId, semester);
        return new PageInfo<>(readingReportListVOS);
    }

    /**
     * 创建阅读报告 —— 自动注入阅读分析数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReadingReportResponseDTO insertReadingReport(ReadingReportCreateDTO readingReportCreateDTO) {
        if (readingReportCreateDTO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 1. 先插入基础记录（userId + semester）
        int result = readingReportMapper.insertReadingReport(readingReportCreateDTO);
        if (result <= 0) {
            throw new RuntimeException("创建阅读报告失败！");
        }
        Long reportId = readingReportMapper.getLastInsertId();

        // 2. 调用阅读分析服务获取偏好数据
        try {
            ReadingAnalysisVO analysis = readingAnalysisService.analyzeReadingBehavior(readingReportCreateDTO.getUserId());
            if (analysis != null) {
                int updateResult = readingReportMapper.updateReportAnalysis(
                        reportId,
                        analysis.getTotalBorrows(),
                        analysis.getFavCategory(),
                        analysis.getAnalysis()
                );
                if (updateResult > 0) {
                    log.info("阅读报告分析数据更新成功: reportId={}, totalBorrow={}, favCategory={}",
                            reportId, analysis.getTotalBorrows(), analysis.getFavCategory());
                }
            }
        } catch (Exception e) {
            // 分析失败不阻塞主流程，仅记录日志
            log.warn("阅读报告分析数据注入失败，报告仍以草稿状态保存: reportId={}", reportId, e);
        }

        // 3. 返回完整报告
        ReadingReportDetailVO detail = readingReportMapper.getReadingReportDetailById(reportId);
        if (detail == null) {
            throw new RuntimeException("获取阅读报告详情失败");
        }
        return convertToResponseDTO(detail);
    }

    /**
     * 获取我的读书报告详情（当前学期优先）
     */
    @Override
    public ReadingReportDetailVO getMyReadingReportDetail(String semester) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("未获取到当前用户信息");
        }

        ReadingReportDetailVO detail = readingReportMapper.getReadingReportByUser(userId, semester);
        if (detail == null) {
            log.info("未找到该用户的阅读报告: userId={}, semester={}", userId, semester);
            return null;
        }
        return detail;
    }

    /**
     * 转换为响应DTO
     */
    private ReadingReportResponseDTO convertToResponseDTO(ReadingReportDetailVO readingReportDetailVO) {
        if (readingReportDetailVO == null) {
            return null;
        }
        ReadingReportResponseDTO readingReportResponseDTO = new ReadingReportResponseDTO();
        readingReportResponseDTO.setId(readingReportDetailVO.getId());
        readingReportResponseDTO.setUserId(readingReportDetailVO.getUserId());
        readingReportResponseDTO.setUserName(readingReportDetailVO.getUserName());
        readingReportResponseDTO.setSemester(readingReportDetailVO.getSemester());
        readingReportResponseDTO.setTotalBorrow(readingReportDetailVO.getTotalBorrow());
        readingReportResponseDTO.setFavCategory(readingReportDetailVO.getFavCategory());
        readingReportResponseDTO.setAnalysisText(readingReportDetailVO.getAnalysisText());
        try {
            if (readingReportDetailVO.getCreateTime() != null && !readingReportDetailVO.getCreateTime().isEmpty()) {
                readingReportResponseDTO.setCreateTime(LocalDateTime.parse(readingReportDetailVO.getCreateTime(), DATE_TIME_FORMATTER));
            }
        } catch (Exception e) {
            log.error("时间转换错误", e);
            throw new RuntimeException("时间转换错误");
        }
        return readingReportResponseDTO;
    }
}