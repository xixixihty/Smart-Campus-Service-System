package com.hxq.smart_campus.controller;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.BorrowCreateDTO;
import com.hxq.smart_campus.entity.dto.BorrowResponseDTO;
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
@RequestMapping("/borrow-records")
@Tag(name = "借阅记录管理")
@RequiredArgsConstructor
@Slf4j
public class BorrowRecordController {
    private final BorrowRecordService borrowRecordService;


    @PostMapping
    @Operation(summary = "创建借阅记录")
    public Result<BorrowResponseDTO> insertBorrowRecord(@RequestBody BorrowCreateDTO borrowCreateDTO) {
        log.info("创建借阅记录，参数：{}", borrowCreateDTO);
        BorrowResponseDTO borrowResponseDTO = borrowRecordService.insertBorrowRecord(borrowCreateDTO);
        return Result.success(borrowResponseDTO);
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "归还图书")
    public Result<Boolean> returnBook(@PathVariable Long id) {
        log.info("归还图书，参数：id={}", id);
        Boolean isSuccess = borrowRecordService.returnBook(id);
        return Result.success(isSuccess);
    }

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

    @GetMapping("/{id}")
    @Operation(summary = "获取借阅记录详情")
    public Result<BorrowRecordDetailVO> getBorrowRecordDetail(@PathVariable Long id) {
        log.info("获取借阅记录详情，参数：id={}", id);
        BorrowRecordDetailVO borrowRecordDetailVO = borrowRecordService.getBorrowRecordDetail(id);
        return Result.success(borrowRecordDetailVO);
    }
    // TODO 从登陆信息中获取个人借阅记录列表
    @GetMapping("/my")
    @Operation(summary = "获取我的借阅记录")
    public Result<PageInfo<BorrowRecordListVO>> getBorrowRecordMyList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam Long userId
    ) {
        log.info("获取我的借阅记录，参数：pageNum={}, pageSize={}, status={}, userId={}", pageNum, pageSize, status, userId);
        PageInfo<BorrowRecordListVO> pageInfo = borrowRecordService.getBorrowRecordMyList(pageNum, pageSize, status, userId);
        return Result.success(pageInfo);
    }

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
}
