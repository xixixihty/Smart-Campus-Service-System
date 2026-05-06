package com.hxq.smart_campus.controller;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.NoticeCreateDTO;
import com.hxq.smart_campus.entity.dto.NoticeResponseDTO;
import com.hxq.smart_campus.entity.dto.NoticeUpdateDTO;
import com.hxq.smart_campus.entity.vo.MyNoticeVO;
import com.hxq.smart_campus.entity.vo.NoticeDetailVO;
import com.hxq.smart_campus.entity.vo.NoticeListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "通知管理模块")
public class NoticeController {
    private final NoticeService noticeService;

    @PostMapping
    @Operation(summary = "发布通知")
    public Result<NoticeResponseDTO> insertNotice(@Valid @RequestBody NoticeCreateDTO noticeCreateDTO) {
        log.info("发布通知: {}", noticeCreateDTO);
        NoticeResponseDTO noticeResponseDTO = noticeService.insertNotice(noticeCreateDTO);
        return Result.success(noticeResponseDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新通知")
    public Result<NoticeResponseDTO> updateNotice(@PathVariable Long id,
                                                  @RequestBody NoticeUpdateDTO noticeUpdateDTO) {
        log.info("更新通知: {}, {}", id, noticeUpdateDTO);
        NoticeResponseDTO noticeResponseDTO = noticeService.updateNotice(id, noticeUpdateDTO);
        return Result.success(noticeResponseDTO);
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "撤回通知")
    public Result<Boolean> withdrawNotice(@PathVariable Long id) {
        log.info("撤回通知: {}", id);
        Boolean result = noticeService.withdrawNotice(id);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知")
    public Result<Boolean> deleteNotice(@PathVariable Long id) {
        log.info("删除通知: {}", id);
        Boolean result = noticeService.deleteNotice(id);
        return Result.success(result);
    }

    @GetMapping
    @Operation(summary = "获取通知列表")
    public Result<PageInfo<NoticeListVO>> getNoticeList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String status
    ) {
        log.info("获取通知列表: {}, {}, {}, {}, {}", pageNum, pageSize, title, targetType, status);
        PageInfo<NoticeListVO> noticeListVO = noticeService.getNoticeList(pageNum, pageSize, title, targetType, status);
        return Result.success(noticeListVO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取通知详情")
    public Result<NoticeDetailVO> getNoticeDetail(@PathVariable Long id) {
        log.info("获取通知详情: {}", id);
        NoticeDetailVO noticeDetailVO = noticeService.getNoticeDetail(id);
        return Result.success(noticeDetailVO);
    }

    @GetMapping("/my")
    @Operation(summary = "获取当前用户通知列表")
    public Result<PageInfo<MyNoticeVO>> getMyNoticeList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        log.info("获取当前用户通知列表: {}, {}", pageNum, pageSize);
        PageInfo<MyNoticeVO> myNoticeVO = noticeService.getMyNoticeList(pageNum, pageSize);
        return Result.success(myNoticeVO);
    }
}
