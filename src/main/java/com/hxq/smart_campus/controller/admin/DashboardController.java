package com.hxq.smart_campus.controller.admin;

import com.hxq.smart_campus.mapper.DashboardMapper;
import com.hxq.smart_campus.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard/admin")
@Tag(name = "工作台统计")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardMapper dashboardMapper;

    @GetMapping("/stats")
    @Operation(summary = "获取工作台统计数据")
    public Result<Map<String, Object>> getStats() {
        log.info("获取工作台统计数据");
        Map<String, Object> stats = new HashMap<>();
        stats.put("collegeCount", dashboardMapper.countColleges());
        stats.put("teacherCount", dashboardMapper.countTeachers());
        stats.put("studentCount", dashboardMapper.countStudents());
        stats.put("courseCount", dashboardMapper.countCourses());
        stats.put("borrowCount", dashboardMapper.countBorrowRecords());
        stats.put("bookCount", dashboardMapper.countBooks());
        stats.put("seatCount", dashboardMapper.countSeats());
        stats.put("leaveCount", dashboardMapper.countLeaveRequests());
        stats.put("noticeCount", dashboardMapper.countPublishedNotices());
        stats.put("studentByCollege", dashboardMapper.studentByCollege());
        stats.put("courseByType", dashboardMapper.courseByType());
        stats.put("teacherByTitle", dashboardMapper.teacherByTitle());
        stats.put("borrowTrend7Days", dashboardMapper.borrowTrend7Days());
        stats.put("leaveByStatus", dashboardMapper.leaveByStatus());
        stats.put("studentTrend7Days", dashboardMapper.studentTrend7Days());
        return Result.success(stats);
    }
}
