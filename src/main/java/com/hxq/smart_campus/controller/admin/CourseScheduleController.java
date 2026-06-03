package com.hxq.smart_campus.controller.admin;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.ConflictCheckDTO;
import com.hxq.smart_campus.entity.dto.CourseScheduleCreateDTO;
import com.hxq.smart_campus.entity.dto.CourseScheduleResponseDTO;
import com.hxq.smart_campus.entity.dto.CourseScheduleUpdateDTO;
import com.hxq.smart_campus.entity.vo.ConflictCheckResultVO;
import com.hxq.smart_campus.entity.vo.CourseScheduleDetailVO;
import com.hxq.smart_campus.entity.vo.CourseScheduleListVO;
import com.hxq.smart_campus.entity.vo.TimetableVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.CourseScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/course-schedules/admin")
@Slf4j
@Tag(name = "排课管理模块")
@RequiredArgsConstructor
@Validated
public class CourseScheduleController {

    private final CourseScheduleService courseScheduleService;

    /**
     * 创建排课
     * @param courseScheduleCreateDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "创建排课")
    public Result<CourseScheduleResponseDTO> insertCourseSchedule(@Valid @RequestBody CourseScheduleCreateDTO courseScheduleCreateDTO) {
        log.info("创建排课: {}", courseScheduleCreateDTO);
        CourseScheduleResponseDTO courseScheduleResponseDTO = courseScheduleService.insertCourseSchedule(courseScheduleCreateDTO);
        return Result.success(courseScheduleResponseDTO);
    }

    /**
     * 更新排课
     * @param courseScheduleUpdateDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "更新排课")
    public Result<CourseScheduleResponseDTO> updateCourseSchedule(@Valid @RequestBody CourseScheduleUpdateDTO courseScheduleUpdateDTO) {
        log.info("更新排课: {}", courseScheduleUpdateDTO);
        CourseScheduleResponseDTO courseScheduleResponseDTO = courseScheduleService.updateCourseSchedule(courseScheduleUpdateDTO);
        return Result.success(courseScheduleResponseDTO);
    }

    /**
     * 删除排课
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除排课")
    public Result<Boolean> deleteCourseSchedule(@PathVariable List<Long> ids) {
        log.info("删除排课: {}", ids);
        boolean result = courseScheduleService.deleteCourseSchedule(ids);
        return Result.success(result);
    }
    /**
     * 查询排课列表
     * @param pageNum
     * @param pageSize
     * @param semesterId
     * @param courseId
     * @param teacherId
     * @param classroomId
     * @param weekDay
     * @param weekNum
     * @return
     */
    @GetMapping
    @Operation(summary = "查询排课列表")
    public Result<PageInfo<CourseScheduleListVO>> getCourseScheduleList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long classroomId,
            @RequestParam(required = false) Integer weekDay,
            @RequestParam(required = false) Integer weekNum
    ) {
        log.info("查询排课列表: pageNum={}, pageSize={}, semesterId={}, courseId={}, teacherId={}, classroomId={}, weekDay={}, weekNum={}",
                pageNum, pageSize, semesterId, courseId, teacherId, classroomId, weekDay, weekNum);
                PageInfo<CourseScheduleListVO> courseScheduleListVO = courseScheduleService.getCourseScheduleList(pageNum, pageSize, semesterId, courseId, teacherId, classroomId, weekDay, weekNum);
                return Result.success(courseScheduleListVO);
    }
    /**
     * 查询排课详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询排课详情")
    public Result<CourseScheduleDetailVO> getCourseScheduleDetail(@PathVariable Long id) {
        log.info("查询排课详情: id={}", id);
        CourseScheduleDetailVO courseScheduleDetailVO = courseScheduleService.getCourseScheduleDetail(id);
        return Result.success(courseScheduleDetailVO);
    }
    /**
     * 冲突检查
     * @param conflictCheckDTO
     * @return
     */
    @PostMapping("/conflict-check")
    @Operation(summary = "检测排课冲突")
    public Result<ConflictCheckResultVO> conflictCheck(@Valid @RequestBody ConflictCheckDTO conflictCheckDTO) {
        log.info("检测排课冲突参数: {}", conflictCheckDTO);
        ConflictCheckResultVO result = courseScheduleService.conflictCheck(conflictCheckDTO);
        return Result.success(result);
    }
    /**
     * 获取课表
     * @param semesterId
     * @param userId
     * @param userType
     * @return
     */
    @GetMapping("/timetable")
    @Operation(summary = "获取课表")
    public Result<List<TimetableVO>> getTimetable(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String userType
    ) {
        log.info("获取课表: semesterId={}, userId={}, userType={}", semesterId, userId, userType);
        List<TimetableVO> result = courseScheduleService.queryTimetable(semesterId, userId, userType);
        return Result.success(result);
    }
}
