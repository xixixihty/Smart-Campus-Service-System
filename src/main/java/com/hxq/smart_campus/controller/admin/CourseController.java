package com.hxq.smart_campus.controller.admin;


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
@RequestMapping("/api/courses/admin")
@Tag(name = "课程管理")
public class CourseController {
    private final CourseService courseService;

    /**
     * 获取课程列表
     * @param pageNum
     * @param pageSize
     * @param courseCode
     * @param courseName
     * @param type
     * @param status
     * @return
     */
    @GetMapping
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

    /**
     * 获取课程详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取课程详情")
    public Result<CourseDetailVO> getCourseDetail(@PathVariable Long id) {
        log.info("查询课程详情，课程ID: {}", id);
        CourseDetailVO courseDetailVO = courseService.getCourseDetail(id);
        return Result.success(courseDetailVO);

    }

    /**
     * 创建课程
     * @param courseCreateDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "创建课程")
    public Result<CourseResponseDTO> insertCourse(@RequestBody CourseCreateDTO courseCreateDTO) {
        log.info("创建课程，课程信息: {}", courseCreateDTO);
        CourseResponseDTO courseResponseDTO = courseService.insertCourse(courseCreateDTO);
        return Result.success(courseResponseDTO);
    }

    /**
     * 更新课程
     * @param courseUpdateDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "更新课程")
    public Result<CourseResponseDTO> updateCourse(@RequestBody CourseUpdateDTO courseUpdateDTO) {
        log.info("更新课程，课程信息: {}", courseUpdateDTO);
        CourseResponseDTO courseResponseDTO = courseService.updateCourse(courseUpdateDTO);
        return Result.success(courseResponseDTO);
    }

    /**
     * 删除课程
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除课程")
    public Result<Boolean> deleteCourse(@PathVariable List<Long> ids) {
        log.info("删除课程，课程ID列表: {}", ids);
        boolean b = courseService.deleteCourse(ids);
        return Result.success(b);
    }
}
