package com.hxq.smart_campus.controller;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.TeacherCreateDTO;
import com.hxq.smart_campus.entity.dto.TeacherResponseDTO;
import com.hxq.smart_campus.entity.dto.TeacherUpdateDTO;
import com.hxq.smart_campus.entity.vo.TeacherDetailVO;
import com.hxq.smart_campus.entity.vo.TeacherListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/teachers")
@Slf4j
@Tag(name = "教师管理模块")
public class TeacherController {
    private final TeacherService teacherService;

    /**
     * 创建教师
     */
    @PostMapping
    @Operation(summary = "创建教师")
    public Result<TeacherResponseDTO> createTeacher(@RequestBody TeacherCreateDTO teacherCreateDTO) {
        log.info("创建教师: {}", teacherCreateDTO);
        TeacherResponseDTO teacherResponseDTO = teacherService.createTeacher(teacherCreateDTO);
        return Result.success(teacherResponseDTO);
    }
    /**
     * 更新教师
     */
    @PutMapping
    @Operation(summary = "更新教师")
    public Result<TeacherResponseDTO> updateTeacher(@RequestBody TeacherUpdateDTO teacherUpdateDTO) {
        log.info("更新教师: {}", teacherUpdateDTO);
        TeacherResponseDTO teacherResponseDTO = teacherService.updateTeacher(teacherUpdateDTO);
        return Result.success(teacherResponseDTO);
    }
    /**
     * 批量删除教师
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "批量删除教师")
    public Result<Boolean> deleteByIds(@PathVariable List<Long> ids) {
        log.info("批量删除教师，ID列表：{}", ids);
        boolean result = teacherService.deleteByIds(ids);
        return Result.success(result);
    }
    /**
     * 获取教师列表
     */
    @GetMapping
    @Operation(summary = "获取教师列表")
    public Result<PageInfo<TeacherListVO>> getTeacherList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String teacherNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String accountStatus
    ) {
        log.info("获取教师列表，参数：{}, {}, {}, {}, {}, {}, {}",
                pageNum, pageSize, teacherNo, name, gender, collegeId, status, accountStatus);
        PageInfo<TeacherListVO> pageInfo = teacherService.getTeacherList(
                pageNum, pageSize, teacherNo, name, gender, collegeId, status, accountStatus);
        return Result.success(pageInfo);
    }

    /**
     * 获取教师详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取教师详情")
    public Result<TeacherDetailVO> getTeacherDetail(@PathVariable Long id) {
        log.info("获取教师详情，ID：{}", id);
        TeacherDetailVO teacherDetailVO = teacherService.getTeacherDetail(id);
        return Result.success(teacherDetailVO);
    }
    /**
     * 重置教师密码
     */
    @Operation(summary = "重置教师密码")
    @PutMapping("/{id}/reset-password")
    public Result<Boolean> reSetPassword(@PathVariable Long id) {
        log.info("重置教师密码，ID：{}", id);
        boolean result = teacherService.reSetPassword(id);
        return Result.success(result);
    }
}
