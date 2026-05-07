package com.hxq.smart_campus.controller.user;

import com.hxq.smart_campus.entity.vo.MakeupExamDetailVO;
import com.hxq.smart_campus.entity.vo.MyMakeupExamVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.MakeupExamService;
import com.hxq.smart_campus.service.impl.MakeupExamServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/make-up-exams/user")
@Tag(name = "补考管理模块")
@Slf4j
@RequiredArgsConstructor
public class MakeUpExamUserController {
    private final MakeupExamService makeupExamService;

    /**
     * 查询我的补考安排列表
     * @param status
     * @return
     */
    @GetMapping("/my")
    @Operation(summary = "查询我的补考安排列表")
    public Result<List<MyMakeupExamVO>> getMyMakeupExamList(
            @RequestParam(required = false) String  status
    )
    {
        log.info("查询我的补考安排列表: status={}", status);
        List<MyMakeupExamVO> myMakeupExamList = makeupExamService.getMyMakeupExamList(status);
        return Result.success(myMakeupExamList);
    }

    /**
     * 查询补考安排详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询补考安排详情")
    public Result<MakeupExamDetailVO> getMakeupExamDetail(@PathVariable Long id) {
        log.info("查询补考安排详情：{}", id);
        MakeupExamDetailVO makeupExamDetailVO = makeupExamService.getMakeupExamDetail(id);
        return Result.success(makeupExamDetailVO);
    }
}
