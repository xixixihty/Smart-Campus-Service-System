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

        // 自动从当前登录用户获取 userId
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("未获取到当前用户信息");
        }
        readingReportCreateDTO.setUserId(userId);

        // 1. 先插入基础记录（userId + semester）
        int result = readingReportMapper.insertReadingReport(readingReportCreateDTO);
        if (result <= 0) {
            throw new RuntimeException("创建阅读报告失败！");
        }
        Long reportId = readingReportMapper.getLastInsertId();

        // 2. 调用阅读分析服务获取偏好数据
        try {
            ReadingAnalysisVO analysis = readingAnalysisService.analyzeReadingBehavior(userId);
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
     * 获取我的读书报告列表（分页）
     */
    @Override
    public PageInfo<ReadingReportListVO> getMyReadingReportList(Integer pageNum, Integer pageSize, String semester) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("未获取到当前用户信息");
        }
        PageHelper.startPage(pageNum, pageSize);
        List<ReadingReportListVO> list = readingReportMapper.getReadingReportList(userId, semester);
        return new PageInfo<>(list);
    }

    /**
     * 根据ID获取阅读报告详情（带归属校验）
     */
    @Override
    public ReadingReportDetailVO getReadingReportDetailById(Long id, Long currentUserId) {
        ReadingReportDetailVO detail = readingReportMapper.getReadingReportDetailById(id);
        if (detail == null) {
            log.info("未找到阅读报告: id={}", id);
            return null;
        }
        // 校验报告归属：仅允许查看自己的报告
        if (!currentUserId.equals(detail.getUserId())) {
            log.warn("用户越权查看阅读报告: currentUserId={}, reportOwner={}, reportId={}",
                    currentUserId, detail.getUserId(), id);
            throw new RuntimeException("无权查看该报告");
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