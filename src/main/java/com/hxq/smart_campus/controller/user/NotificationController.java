package com.hxq.smart_campus.controller.user;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.vo.NotificationVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "通知管理模块")
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/unread")
    @Operation(summary = "获取未读通知列表")
    public Result<List<NotificationVO>> getUnreadList() {
        List<NotificationVO> list = notificationService.getUnreadList();
        return Result.success(list);
    }

    @GetMapping
    @Operation(summary = "获取通知列表")
    public Result<PageInfo<NotificationVO>> getNotificationList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type
    ) {
        PageInfo<NotificationVO> pageInfo = notificationService.getNotificationList(pageNum, pageSize, type);
        return Result.success(pageInfo);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数量")
    public Result<Long> getUnreadCount() {
        Long count = notificationService.getUnreadCount();
        return Result.success(count);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记通知为已读")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部标记为已读")
    public Result<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return Result.success();
    }
}