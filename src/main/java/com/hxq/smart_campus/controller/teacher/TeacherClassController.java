package com.hxq.smart_campus.controller.teacher;

import com.hxq.smart_campus.entity.vo.ClassDetailVO;
import com.hxq.smart_campus.entity.vo.StudentBasicVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.ClassService;
import com.hxq.smart_campus.service.SemesterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
@Slf4j
@Tag(name = "教师班级管理模块")
@RequiredArgsConstructor
@Validated
public class TeacherClassController {

    private final ClassService classService;
    private final SemesterService semesterService;

    @GetMapping("/classes")
    @Operation(summary = "查看所授课班级列表")
    public Result<List<ClassDetailVO>> getTeachingClasses(@RequestParam(required = false) Long semesterId) {
        var currentSemester = semesterService.getCurrentSemester();
        Long currentSemesterId = currentSemester.getId();
        log.info("教师查询授课班级列表: 前端传入semesterId={}, 强制使用当前学期: {} (ID={})",
                semesterId, currentSemester.getName(), currentSemesterId);
        List<ClassDetailVO> classes = classService.getTeachingClasses(currentSemesterId);
        return Result.success(classes);
    }

    @GetMapping("/class/{classId}/students")
    @Operation(summary = "查看班级学生花名册")
    public Result<List<StudentBasicVO>> getClassStudents(@PathVariable Long classId) {
        log.info("教师查询班级学生: classId={}", classId);
        List<StudentBasicVO> students = classService.getClassStudents(classId);
        return Result.success(students);
    }

    @GetMapping("/class/{classId}")
    @Operation(summary = "查看班级详情")
    public Result<ClassDetailVO> getClassDetail(@PathVariable Long classId) {
        log.info("教师查询班级详情: classId={}", classId);
        ClassDetailVO detail = classService.getClassDetail(classId);
        return Result.success(detail);
    }
}