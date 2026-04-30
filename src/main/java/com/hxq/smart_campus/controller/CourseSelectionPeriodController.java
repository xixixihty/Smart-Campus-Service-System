package com.hxq.smart_campus.controller;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.CourseSelectionPeriodCreateDTO;
import com.hxq.smart_campus.entity.dto.CourseSelectionPeriodResponseDTO;
import com.hxq.smart_campus.entity.dto.CourseSelectionPeriodUpdateDTO;
import com.hxq.smart_campus.entity.vo.CourseSelectionPeriodDetailVO;
import com.hxq.smart_campus.entity.vo.CourseSelectionPeriodListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.CourseSelectionPeriodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course-selection-periods")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "选课时间段管理模块")
public class CourseSelectionPeriodController {
    private final CourseSelectionPeriodService courseSelectionPeriodService;

    private final RedisTemplate redisTemplate;
    /**
     * 创建选课时间段
     * @param courseSelectionPeriodCreateDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "创建选课时间段")
    public Result<CourseSelectionPeriodResponseDTO> insertCourseSelectionPeriod(@RequestBody CourseSelectionPeriodCreateDTO courseSelectionPeriodCreateDTO) {
        log.info("创建选课时间段: {}", courseSelectionPeriodCreateDTO);
        CourseSelectionPeriodResponseDTO courseSelectionPeriodResponseDTO = courseSelectionPeriodService.insertCourseSelectionPeriod(courseSelectionPeriodCreateDTO);
        return Result.success(courseSelectionPeriodResponseDTO);
    }

    /**
     * 更新选课时间段
     * @param courseSelectionPeriodUpdateDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "更新选课时间段")
    public Result<CourseSelectionPeriodResponseDTO> updateCourseSelectionPeriod(@RequestBody CourseSelectionPeriodUpdateDTO courseSelectionPeriodUpdateDTO) {
        log.info("更新选课时间段: {}", courseSelectionPeriodUpdateDTO);
        CourseSelectionPeriodResponseDTO courseSelectionPeriodResponseDTO = courseSelectionPeriodService.updateCourseSelectionPeriod(courseSelectionPeriodUpdateDTO);
        return Result.success(courseSelectionPeriodResponseDTO);
    }

    /**
     * 删除选课时间段
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除选课时间段")
    public Result<Boolean> deleteCourseSelectionPeriod(@PathVariable List<Long> ids) {
        log.info("删除选课时间段，ID：{}", ids);
        boolean result = courseSelectionPeriodService.deleteCourseSelectionPeriod(ids);
        return Result.success(result);
    }

    /**
     * 获取选课时间段列表
     * @param pageNum
     * @param pageSize
     * @param semesterId
     * @return
     */
    @GetMapping
    @Operation(summary = "获取选课时间段列表")
    public Result<PageInfo<CourseSelectionPeriodListVO>> getCourseSelectionPeriodList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long semesterId
    ) {
        log.info("获取选课时间段列表，参数：pageNum={}, pageSize={}, semesterId={}", pageNum, pageSize, semesterId);
        PageInfo<CourseSelectionPeriodListVO> pageInfo = courseSelectionPeriodService.getCourseSelectionPeriodList(pageNum, pageSize, semesterId);
        return Result.success(pageInfo);
    }

    /**
     * 获取选课时间段详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取选课时间段详情")
    public Result<CourseSelectionPeriodDetailVO> getCourseSelectionPeriodDetail(@PathVariable Long id) {
        log.info("获取选课时间段详情，ID：{}", id);
        CourseSelectionPeriodDetailVO courseSelectionPeriodDetailVO = courseSelectionPeriodService.getCourseSelectionPeriodDetail(id);
        return Result.success(courseSelectionPeriodDetailVO);
    }



}
