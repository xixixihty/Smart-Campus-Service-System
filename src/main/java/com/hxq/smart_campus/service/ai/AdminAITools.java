package com.hxq.smart_campus.service.ai;

import com.hxq.smart_campus.entity.vo.ScoreEntryListVO;
import com.hxq.smart_campus.entity.vo.SeatReservationListVO;
import com.hxq.smart_campus.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminAITools {

    private final DashboardMapper dashboardMapper;
    private final ScoreEntryMapper scoreEntryMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final SeatReservationMapper seatReservationMapper;

    @Tool(description = "获取校园整体统计数据。当用户询问校园概况、学生数量、教师数量、课程数量等统计信息时调用此工具。")
    public String getCampusStatistics() {
        log.info("Tool调用: getCampusStatistics");
        long students = dashboardMapper.countStudents();
        long teachers = dashboardMapper.countTeachers();
        long courses = dashboardMapper.countCourses();
        long books = dashboardMapper.countBooks();
        long seats = dashboardMapper.countSeats();
        long borrowRecords = dashboardMapper.countBorrowRecords();
        long leaveRequests = dashboardMapper.countLeaveRequests();
        long notices = dashboardMapper.countPublishedNotices();

        return String.format(
                "校园数据统计：\n" +
                "- 学生总数：%d\n" +
                "- 教师总数：%d\n" +
                "- 课程总数：%d\n" +
                "- 图书总数：%d\n" +
                "- 座位总数：%d\n" +
                "- 借阅记录数：%d\n" +
                "- 请假申请数：%d\n" +
                "- 已发布通知数：%d",
                students, teachers, courses, books, seats, borrowRecords, leaveRequests, notices);
    }

    @Tool(description = "获取学生学院分布统计。当用户询问学生分布、各学院人数时调用此工具。")
    public String getStudentDistribution() {
        log.info("Tool调用: getStudentDistribution");
        List<Map<String, Object>> distribution = dashboardMapper.studentByCollege();
        if (distribution.isEmpty()) {
            return "暂无学生分布数据。";
        }
        return distribution.stream()
                .map(m -> String.format("学院：%s | 学生数：%s", m.get("name"), m.get("value")))
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "获取教师职称分布统计。当用户询问教师职称、教师结构时调用此工具。")
    public String getTeacherDistribution() {
        log.info("Tool调用: getTeacherDistribution");
        List<Map<String, Object>> distribution = dashboardMapper.teacherByTitle();
        if (distribution.isEmpty()) {
            return "暂无教师分布数据。";
        }
        return distribution.stream()
                .map(m -> String.format("职称：%s | 人数：%s", m.get("name"), m.get("value")))
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "获取课程类型分布统计。当用户询问课程类型、课程结构时调用此工具。")
    public String getCourseDistribution() {
        log.info("Tool调用: getCourseDistribution");
        List<Map<String, Object>> distribution = dashboardMapper.courseByType();
        if (distribution.isEmpty()) {
            return "暂无课程分布数据。";
        }
        return distribution.stream()
                .map(m -> String.format("类型：%s | 数量：%s", m.get("name"), m.get("value")))
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "查询班级成绩数据。当用户询问班级成绩、某班考试情况时调用此工具。")
    public String getClassScores(@ToolParam(description = "班级ID") Long classId,
                                 @ToolParam(description = "学期ID（可选）", required = false) Long semesterId) {
        log.info("Tool调用: getClassScores, classId={}, semesterId={}", classId, semesterId);
        List<ScoreEntryListVO> classScores = scoreEntryMapper.getScoreListByClassId(classId, semesterId);
        if (classScores.isEmpty()) {
            return "该班级暂无成绩数据。";
        }
        return classScores.stream()
                .map(s -> String.format("学生：%s | 学号：%s | 课程：%s | 总分：%s",
                        s.getStudentName(),
                        s.getStudentNo(),
                        s.getCourseName(),
                        s.getTotalScore() != null ? s.getTotalScore() : "--"))
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "获取近7天借阅趋势。当用户询问借阅趋势、借阅情况时调用此工具。")
    public String getBorrowTrend() {
        log.info("Tool调用: getBorrowTrend");
        List<Map<String, Object>> trend = dashboardMapper.borrowTrend7Days();
        if (trend.isEmpty()) {
            return "暂无借阅趋势数据。";
        }
        return trend.stream()
                .map(m -> String.format("日期：%s | 借阅量：%s", m.get("name"), m.get("value")))
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "获取座位使用率统计。当用户询问座位使用情况、座位利用率时调用此工具。")
    public String getSeatUtilization() {
        log.info("Tool调用: getSeatUtilization");
        long totalSeats = dashboardMapper.countSeats();
        List<SeatReservationListVO> inUseReservations = seatReservationMapper.getSeatReservationList(
                null, null, null, "使用中");
        List<SeatReservationListVO> awayReservations = seatReservationMapper.getSeatReservationList(
                null, null, null, "暂离");
        long usedSeats = inUseReservations.size() + awayReservations.size();
        double rate = totalSeats > 0 ? (double) usedSeats / totalSeats * 100 : 0;
        return String.format("座位总数：%d | 使用中：%d | 暂离：%d | 总占用：%d | 使用率：%.1f%%",
                totalSeats, inUseReservations.size(), awayReservations.size(), usedSeats, rate);
    }

    @Tool(description = "获取请假状态分布统计。当用户询问请假情况时调用此工具。")
    public String getLeaveStatistics() {
        log.info("Tool调用: getLeaveStatistics");
        List<Map<String, Object>> stats = dashboardMapper.leaveByStatus();
        if (stats.isEmpty()) {
            return "暂无请假数据。";
        }
        return stats.stream()
                .map(m -> String.format("状态：%s | 数量：%s", m.get("name"), m.get("value")))
                .collect(Collectors.joining("\n"));
    }
}