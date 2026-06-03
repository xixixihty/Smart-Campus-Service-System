package com.hxq.smart_campus.controller.teacher;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.vo.MyNoticeVO;
import com.hxq.smart_campus.entity.vo.NoticeDetailVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher")
@Slf4j
@Tag(name = "教师通知模块")
@RequiredArgsConstructor
@Validated
public class TeacherNoticeController {

    private final NoticeService noticeService;

    @GetMapping("/notices")
    @Operation(summary = "获取我的通知列表")
    public Result<PageInfo<MyNoticeVO>> getMyNotices(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("教师查询通知列表: pageNum={}, pageSize={}", pageNum, pageSize);
        PageInfo<MyNoticeVO> notices = noticeService.getMyNoticeList(pageNum, pageSize);
        return Result.success(notices);
    }

    @GetMapping("/notice/{id}")
    @Operation(summary = "获取通知详情")
    public Result<NoticeDetailVO> getNoticeDetail(@PathVariable Long id) {
        log.info("教师查询通知详情: id={}", id);
        NoticeDetailVO detail = noticeService.getNoticeDetail(id);
        return Result.success(detail);
    }

    @GetMapping("/notice/unread-count")
    @Operation(summary = "获取未读通知数")
    public Result<Long> getUnreadCount() {
        Long count = noticeService.getUnreadCount();
        return Result.success(count);
    }
}