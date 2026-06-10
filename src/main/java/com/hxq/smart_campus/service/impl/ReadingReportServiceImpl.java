package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.ReadingReportCreateDTO;
import com.hxq.smart_campus.entity.dto.ReadingReportResponseDTO;
import com.hxq.smart_campus.entity.vo.ReadingReportDetailVO;
import com.hxq.smart_campus.entity.vo.ReadingReportListVO;
import com.hxq.smart_campus.mapper.ReadingReportMapper;
import com.hxq.smart_campus.service.ReadingReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.hxq.smart_campus.constant.MessageConstant.DATE_TIME_FORMATTER;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadingReportServiceImpl implements ReadingReportService {

    private final ReadingReportMapper readingReportMapper;



    /**
     * 获取阅读报告列表
     * @param pageNum
     * @param pageSize
     * @param userId
     * @param semester
     * @return
     */
    @Override
    public PageInfo<ReadingReportListVO> getReadingReportList(Integer pageNum, Integer pageSize, Long userId, String semester) {
        PageHelper.startPage(pageNum, pageSize);
        List<ReadingReportListVO> readingReportListVOS = readingReportMapper.getReadingReportList(userId, semester);
        return new PageInfo<>(readingReportListVOS);
    }



    /**
     * 创建阅读报告
     * @param readingReportCreateDTO
     * @return
     */
    @Override
    public ReadingReportResponseDTO insertReadingReport(ReadingReportCreateDTO readingReportCreateDTO) {
        if (readingReportCreateDTO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        int result = readingReportMapper.insertReadingReport(readingReportCreateDTO);
        if (result <= 0) {
            throw new RuntimeException("创建阅读报告失败！");
        }
        // 获取最近插入的ID
        Long id = readingReportMapper.getLastInsertId();
        ReadingReportDetailVO readingReportDetailVO = readingReportMapper.getReadingReportDetailById(id);
        if (readingReportDetailVO == null) {
            throw new RuntimeException("获取最近插入的ID失败！");
        }
        return convertToResponseDTO(readingReportDetailVO);
    }


    /**
     * 获取我的读书报告详情
     * @param semester
     * @return
     */
    @Override
    public ReadingReportDetailVO getMyReadingReportDetail(String semester) {
        return null;
    }

    /**
     * 转换为响应DTO
     * @param readingReportDetailVO
     * @return
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
            log.error("时间转换错误", e); // Log the error with a message
            throw new RuntimeException("时间转换错误");
        }
        return readingReportResponseDTO;
    }
}
