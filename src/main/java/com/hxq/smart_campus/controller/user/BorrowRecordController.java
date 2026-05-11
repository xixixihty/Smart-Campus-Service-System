package com.hxq.smart_campus.controller.user;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.BorrowCreateDTO;
import com.hxq.smart_campus.entity.dto.BorrowResponseDTO;
import com.hxq.smart_campus.entity.vo.BorrowRecordDetailVO;
import com.hxq.smart_campus.entity.vo.BorrowRecordListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.BorrowRecordService;
import com.hxq.smart_campus.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrow-records/user")
@Tag(name = "借阅记录管理")
@RequiredArgsConstructor
@Slf4j
public class BorrowRecordController {
    private final BorrowRecordService borrowRecordService;

    /**
     * 创建借阅记录
     * @param borrowCreateDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "创建借阅记录")
    public Result<BorrowResponseDTO> insertBorrowRecord(@RequestBody BorrowCreateDTO borrowCreateDTO) {
        log.info("创建借阅记录，参数：{}", borrowCreateDTO);
        BorrowResponseDTO borrowResponseDTO = borrowRecordService.insertBorrowRecord(borrowCreateDTO);
        return Result.success(borrowResponseDTO);
    }

    /**
     * 归还图书
     * @param id
     * @return
     */
    @PostMapping("/{id}/return")
    @Operation(summary = "归还图书")
    public Result<Boolean> returnBook(@PathVariable Long id) {
        log.info("归还图书，参数：id={}", id);
        Boolean isSuccess = borrowRecordService.returnBook(id);
        return Result.success(isSuccess);
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
    /**
     * 获取我的借阅记录
     * @param pageNum
     * @param pageSize
     * @param status
     * @param userId
     * @return
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的借阅记录")
    public Result<PageInfo<BorrowRecordListVO>> getBorrowRecordMyList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId
    ) {
        if (userId == null) {
            userId = SecurityUtils.getCurrentUserId();
        }
        log.info("获取我的借阅记录，参数：pageNum={}, pageSize={}, status={}, userId={}", pageNum, pageSize, status, userId);
        PageInfo<BorrowRecordListVO> pageInfo = borrowRecordService.getBorrowRecordMyList(pageNum, pageSize, status, userId);
        return Result.success(pageInfo);
    }


}
