package com.hxq.smart_campus.controller;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.MakeupExamCreateDTO;
import com.hxq.smart_campus.entity.dto.MakeupExamResponseDTO;
import com.hxq.smart_campus.entity.dto.MakeupExamScoreDTO;
import com.hxq.smart_campus.entity.dto.MakeupExamUpdateDTO;
import com.hxq.smart_campus.entity.vo.MakeupExamDetailVO;
import com.hxq.smart_campus.entity.vo.MakeupExamListVO;
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
@RequestMapping("/api/makeup-exams")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "补考管理模块")
public class MakeUpExamController {
    private final MakeupExamService makeupExamService;


    /**
     * 新增补考安排
     * @param makeupExamCreateDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "新增补考安排")
    public Result<MakeupExamResponseDTO> insertMakeupExam(@RequestBody MakeupExamCreateDTO makeupExamCreateDTO) {
        log.info("新增补考安排：{}", makeupExamCreateDTO);
        MakeupExamResponseDTO makeupExamResponseDTO = makeupExamService.insertMakeupExam(makeupExamCreateDTO);
        return Result.success(makeupExamResponseDTO);
    }

    /**
     * 更新补考安排
     * @param makeupExamUpdateDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "更新补考安排")
    public Result<MakeupExamResponseDTO> updateMakeupExam(@RequestBody MakeupExamUpdateDTO makeupExamUpdateDTO) {
        log.info("更新补考安排：{}", makeupExamUpdateDTO);
        MakeupExamResponseDTO makeupExamResponseDTO = makeupExamService.updateMakeupExam(makeupExamUpdateDTO);
        return Result.success(makeupExamResponseDTO);
    }

    /**
     * 删除补考安排
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除补考安排")
    public Result<Boolean> deleteMakeupExam(@PathVariable Long id) {
        log.info("删除补考安排：{}", id);
        boolean b = makeupExamService.deleteMakeupExam(id);
        return Result.success(b);
    }

    /**
     * 查询补考安排列表
     * @param pageNum
     * @param pageSize
     * @param scoreEntryId
     * @param status
     * @return
     */
    @GetMapping
    @Operation(summary = "查询补考安排列表")
    public Result<PageInfo<MakeupExamListVO>> getMakeupExam(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long scoreEntryId,
            @RequestParam(required = false) String  status
    ) {
        log.info("查询补考安排列表: pageNum={}, pageSize={}, scoreEntryId={}, status={}", pageNum, pageSize, scoreEntryId, status);
        PageInfo<MakeupExamListVO> makeupExamListVO = makeupExamService.getMakeupExamList(pageNum, pageSize, scoreEntryId, status);
        return Result.success(makeupExamListVO);
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
     * 录入补考成绩
     * @param makeupExamScoreDTO
     * @return
     */
    @PutMapping("/score")
    public Result<Boolean> insertMakeupExamScore(@RequestBody MakeupExamScoreDTO makeupExamScoreDTO) {
        log.info("录入补考成绩：{}", makeupExamScoreDTO);
        boolean b = makeupExamService.insertMakeupExamScore(makeupExamScoreDTO);
        return Result.success(b);
    }
}
