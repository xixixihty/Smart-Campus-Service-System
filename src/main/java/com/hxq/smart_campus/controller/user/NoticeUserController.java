package com.hxq.smart_campus.controller.user;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.vo.MyNoticeVO;
import com.hxq.smart_campus.entity.vo.NoticeDetailVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户通知模块")
public class NoticeUserController {
    private final NoticeService noticeService;

    /**
     * 获取通知详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取通知详情")
    public Result<NoticeDetailVO> getNoticeDetail(@PathVariable Long id) {
        log.info("获取通知详情: {}", id);
        NoticeDetailVO noticeDetailVO = noticeService.getNoticeDetail(id);
        return Result.success(noticeDetailVO);
    }

    /**
     * 获取我的通知列表
     * @param pageNum
     * @param pageSize
     * @return
     */
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

    /**
     * 获取未读通知数量
     * @return
     */
    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数量")
    public Result<Long> getUnreadCount() {
        log.info("获取未读通知数量");
        Long count = noticeService.getUnreadCount();
        return Result.success(count);
    }
}
