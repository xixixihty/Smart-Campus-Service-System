package com.hxq.smart_campus.controller.admin;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.BorrowStatisticsDTO;
import com.hxq.smart_campus.entity.vo.BorrowRecordDetailVO;
import com.hxq.smart_campus.entity.vo.BorrowRecordListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.BorrowRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/borrow-records/admin")
@Tag(name = "管理员借阅记录管理")
@RequiredArgsConstructor
@Slf4j
public class AdminBorrowRecordController {
    private final BorrowRecordService borrowRecordService;

    /**
     * 获取借阅记录列表
     * @param pageNum
     * @param pageSize
     * @param userId
     * @param bookId
     * @param status
     * @return
     */
    @GetMapping
    @Operation(summary = "获取借阅记录列表")
    public Result<PageInfo<BorrowRecordListVO>> getBorrowRecordList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) String status
    ) {
        log.info("获取借阅记录列表，参数：pageNum={}, pageSize={}, userId={}, bookId={}, status={}", pageNum, pageSize, userId, bookId, status);
        PageInfo<BorrowRecordListVO> pageInfo = borrowRecordService.getBorrowRecordList(pageNum, pageSize, userId, bookId, status);
        return Result.success(pageInfo);
    }

    /**
     * 获取借阅统计信息
     * @param userId
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取借阅记录统计信息")
    public Result<BorrowStatisticsDTO> getBorrowRecordStatistics(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        log.info("获取借阅记录统计信息，参数：userId={}, startDate={}, endDate={}", userId, startDate, endDate);
        BorrowStatisticsDTO statistics = borrowRecordService.getBorrowRecordStatistics(userId, startDate, endDate);
        return Result.success(statistics);
    }

    /**
     * 获取借阅记录详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取借阅记录详情")
    public Result<BorrowRecordDetailVO> getBorrowRecordDetail(@PathVariable Long id) {
        log.info("获取借阅记录详情，参数：id={}", id);
        BorrowRecordDetailVO borrowRecordDetailVO = borrowRecordService.getBorrowRecordDetail(id);
        return Result.success(borrowRecordDetailVO);
    }
}
