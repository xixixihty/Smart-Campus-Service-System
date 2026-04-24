package com.hxq.smart_campus.controller;


import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.CourseCreateDTO;
import com.hxq.smart_campus.entity.dto.CourseResponseDTO;
import com.hxq.smart_campus.entity.dto.CourseUpdateDTO;
import com.hxq.smart_campus.entity.vo.CourseDetailVO;
import com.hxq.smart_campus.entity.vo.CourseListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/courses")
@Tag(name = "课程管理")
public class CourseController {
    private final CourseService courseService;

    @RequestMapping
    @Operation(summary = "获取课程列表")
    public Result<PageInfo<CourseListVO>> getCourseList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String courseCode,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status
    ) {
        log.info("Getting course list with pageNum: {}, pageSize: {}, courseCode: {}, courseName: {}, type: {}, status: {}",
                pageNum, pageSize, courseCode, courseName, type, status);
        PageInfo<CourseListVO> pageInfo = courseService.getCourseList(pageNum, pageSize, courseCode, courseName, type, status);
        return Result.success(pageInfo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取课程详情")
    public Result<CourseDetailVO> getCourseDetail(@PathVariable Long id) {
        log.info("查询课程详情，课程ID: {}", id);
        CourseDetailVO courseDetailVO = courseService.getCourseDetail(id);
        return Result.success(courseDetailVO);

    }

    @PostMapping
    @Operation(summary = "创建课程")
    public Result<CourseResponseDTO> insertCourse(@RequestBody CourseCreateDTO courseCreateDTO) {
        log.info("创建课程，课程信息: {}", courseCreateDTO);
        CourseResponseDTO courseResponseDTO = courseService.insertCourse(courseCreateDTO);
        return Result.success(courseResponseDTO);
    }

    @PutMapping
    @Operation(summary = "更新课程")
    public Result<CourseResponseDTO> updateCourse(@RequestBody CourseUpdateDTO courseUpdateDTO) {
        log.info("更新课程，课程信息: {}", courseUpdateDTO);
        CourseResponseDTO courseResponseDTO = courseService.updateCourse(courseUpdateDTO);
        return Result.success(courseResponseDTO);
    }

    @DeleteMapping("/{ids}")
    @Operation(summary = "删除课程")
    public Result<Boolean> deleteCourse(@PathVariable List<Long> ids) {
        log.info("删除课程，课程ID列表: {}", ids);
        boolean b = courseService.deleteCourse(ids);
        return Result.success(b);
    }
}
