package com.hxq.smart_campus.controller.user;

import com.hxq.smart_campus.entity.vo.TimetableVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.CourseScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/course-schedules/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户课表查询模块")
public class CourseScheduleUserController {
    private final CourseScheduleService courseScheduleService;

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
