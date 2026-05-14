package com.hxq.smart_campus.controller.user;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.CourseSelectionCreateDTO;
import com.hxq.smart_campus.entity.dto.CourseSelectionResponseDTO;
import com.hxq.smart_campus.entity.vo.AvailableCourseVO;
import com.hxq.smart_campus.entity.vo.CourseSelectionListVO;
import com.hxq.smart_campus.entity.vo.MyCourseSelectionVO;
import com.hxq.smart_campus.entity.vo.SelectionTimeRedisVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.CourseSelectionPeriodService;
import com.hxq.smart_campus.service.CourseSelectionService;
import com.hxq.smart_campus.service.SemesterService;
import com.hxq.smart_campus.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/course-selections/user")
@Tag(name = "选课管理模块")
@RequiredArgsConstructor
public class CourseSelectionController {
    private final CourseSelectionService courseSelectionService;
    private final CourseSelectionPeriodService courseSelectionPeriodService;
    private final SemesterService semesterService;


    /**
     * 学生选课
     * @param courseSelectionCreateDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "学生选课")
    public Result<CourseSelectionResponseDTO> selectCourse(@RequestBody CourseSelectionCreateDTO courseSelectionCreateDTO) {
        log.info("学生选课，选课信息：{}", courseSelectionCreateDTO);
        // 从登录信息中获取用户ID
        Long studentId = SecurityUtils.getCurrentUserId();
        courseSelectionCreateDTO.setStudentId(studentId);
        // 方案C：前端未传semesterId时，自动填充当前学期ID
        if (courseSelectionCreateDTO.getSemesterId() == null) {
            var currentSemester = semesterService.getCurrentSemester();
            if (currentSemester == null) {
                return Result.error("当前学期未设置，请联系管理员");
            }
            courseSelectionCreateDTO.setSemesterId(currentSemester.getId());
            log.info("前端未传学期ID，自动填充当前学期ID：{}", currentSemester.getId());
        }
        CourseSelectionResponseDTO courseSelectionResponseDTO = courseSelectionService.selectCourse(courseSelectionCreateDTO);
        return Result.success(courseSelectionResponseDTO);
    }
    /**
     * 学生选课
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "学生退课")
    public Result<Boolean> dropCourse(@PathVariable Long id) {
        log.info("学生退课，id：{}", id);
        boolean b = courseSelectionService.dropCourse(id);
        return Result.success(b);
    }

    /**
     * 查询选课列表
     * @param pageNum
     * @param pageSize
     * @param studentId
     * @param courseId
     * @param semesterId
     * @param status
     * @return
     */
    @GetMapping
    @Operation(summary = "查询选课列表")
    public Result<PageInfo<CourseSelectionListVO>> getCourseSelectionList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) String status
    ) {
        log.info("查询选课列表: pageNum={}, pageSize={}, studentId={}, courseId={}, semesterId={}, status={}");
        PageInfo<CourseSelectionListVO> courseSelectionListVO = courseSelectionService.getCourseSelectionList(pageNum, pageSize, studentId, courseId, semesterId, status);
        return Result.success(courseSelectionListVO);
    }

    /**
     * 查询我的选课课程列表
     * @param semesterId
     * @param status
     * @return
     */
    @GetMapping("/my")
    @Operation(summary = "查询我的选课课程列表")
    public Result<List<MyCourseSelectionVO>> getMyCourseSelectionList(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) String status
    ) {
        log.info("查询我的选课课程列表: semesterId={}, status={}");
        List<MyCourseSelectionVO> courseSelectionListVO = courseSelectionService.getMyCourseSelectionList(semesterId, status);
        return Result.success(courseSelectionListVO);
    }

    /**
     * 查询可选课程
     * @param semesterId
     * @return
     */
    @GetMapping("/available")
    @Operation(summary = "查询可选课程")
    public Result<List<AvailableCourseVO>> getAvailableCourseList(
            @RequestParam(required = false) Long semesterId
    ) {
        log.info("查询可选课程列表: semesterId={}");
        List<AvailableCourseVO> courseSelectionListVO = courseSelectionService.getAvailableCourseList(semesterId);
        return Result.success(courseSelectionListVO);
    }

    /**
     * 获取当前学期的选课时间段
     * @return
     */
    @GetMapping("/period")
    @Operation(summary = "获取当前学期的选课时间段")
    public Result<SelectionTimeRedisVO> getCurrentSemesterCourseSelectionPeriod() {
        log.info("获取当前学期的选课时间段");
        try {
            SelectionTimeRedisVO selectionTimeRedisVO = courseSelectionPeriodService.getCurrentSemesterCourseSelectionPeriod();
            return Result.success(selectionTimeRedisVO);
        } catch (IllegalArgumentException e) {
            log.warn("获取选课时间段失败: {}", e.getMessage());
            return Result.success(null);
        }
    }
}
