package com.hxq.smart_campus.controller.user;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.ReadingReportCreateDTO;
import com.hxq.smart_campus.entity.dto.ReadingReportResponseDTO;
import com.hxq.smart_campus.entity.vo.ReadingReportDetailVO;
import com.hxq.smart_campus.entity.vo.ReadingReportListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.ReadingReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reading-reports/user")
@Tag(name = "读书报告管理")
@RequiredArgsConstructor
@Slf4j
public class ReadingReportController {
    private final ReadingReportService readingReportService;



    /**
     * 新增读书报告
     * @param readingReportCreateDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "新增读书报告")
    public Result<ReadingReportResponseDTO> insertReadingReport(@RequestBody ReadingReportCreateDTO readingReportCreateDTO) {
        log.info("新增读书报告，参数：{}", readingReportCreateDTO);
        ReadingReportResponseDTO readingReportResponseDTO = readingReportService.insertReadingReport(readingReportCreateDTO);
        return Result.success(readingReportResponseDTO);
    }

    /**
     * 获取读书报告列表
     * @param pageNum
     * @param pageSize
     * @param userId
     * @param semester
     * @return
     */
    @GetMapping
    @Operation(summary = "获取读书报告列表")
    public Result<PageInfo<ReadingReportListVO>> getReadingReportList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String semester
    ) {
        log.info("获取读书报告列表，参数：{}", pageNum, pageSize, userId, semester);
        PageInfo<ReadingReportListVO> readingReportListVOPageInfo = readingReportService.getReadingReportList(pageNum, pageSize, userId, semester);
        return Result.success(readingReportListVOPageInfo);
    }

    // TODO: 从登陆信息中获取用户信息
    @GetMapping("/my")
    @Operation(summary = "获取我的读书报告详情")
    public Result<ReadingReportDetailVO> getMyReadingReportDetail(
            @RequestParam(required = false) String semester
    ) {
        log.info("获取我的读书报告详情，参数：{}", semester);
        ReadingReportDetailVO readingReportDetailVO = readingReportService.getMyReadingReportDetail(semester);
        return Result.success(readingReportDetailVO);
    }
}
