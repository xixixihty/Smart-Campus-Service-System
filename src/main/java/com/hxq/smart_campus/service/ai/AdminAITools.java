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

        return MarkdownFormatter.header("校园数据统计") + "\n\n"
                + MarkdownFormatter.toKeyValueList(
                        "学生总数", String.valueOf(students),
                        "教师总数", String.valueOf(teachers),
                        "课程总数", String.valueOf(courses),
                        "图书总数", String.valueOf(books),
                        "座位总数", String.valueOf(seats),
                        "借阅记录数", String.valueOf(borrowRecords),
                        "请假申请数", String.valueOf(leaveRequests),
                        "已发布通知数", String.valueOf(notices));
    }

    @Tool(description = "获取学生学院分布统计。当用户询问学生分布、各学院人数时调用此工具。")
    public String getStudentDistribution() {
        log.info("Tool调用: getStudentDistribution");
        List<Map<String, Object>> distribution = dashboardMapper.studentByCollege();
        if (distribution.isEmpty()) {
            return MarkdownFormatter.noData("学生分布");
        }
        return MarkdownFormatter.header("学生学院分布") + "\n\n"
                + MarkdownFormatter.mapListToKeyValue(distribution, "name", "value");
    }

    @Tool(description = "获取教师职称分布统计。当用户询问教师职称、教师结构时调用此工具。")
    public String getTeacherDistribution() {
        log.info("Tool调用: getTeacherDistribution");
        List<Map<String, Object>> distribution = dashboardMapper.teacherByTitle();
        if (distribution.isEmpty()) {
            return MarkdownFormatter.noData("教师分布");
        }
        return MarkdownFormatter.header("教师职称分布") + "\n\n"
                + MarkdownFormatter.mapListToKeyValue(distribution, "name", "value");
    }

    @Tool(description = "获取课程类型分布统计。当用户询问课程类型、课程结构时调用此工具。")
    public String getCourseDistribution() {
        log.info("Tool调用: getCourseDistribution");
        List<Map<String, Object>> distribution = dashboardMapper.courseByType();
        if (distribution.isEmpty()) {
            return MarkdownFormatter.noData("课程分布");
        }
        return MarkdownFormatter.header("课程类型分布") + "\n\n"
                + MarkdownFormatter.mapListToKeyValue(distribution, "name", "value");
    }

    @Tool(description = "查询班级成绩数据。当用户询问班级成绩、某班考试情况时调用此工具。")
    public String getClassScores(@ToolParam(description = "班级ID") Long classId,
                                 @ToolParam(description = "学期ID（可选）", required = false) Long semesterId) {
        log.info("Tool调用: getClassScores, classId={}, semesterId={}", classId, semesterId);
        List<ScoreEntryListVO> classScores = scoreEntryMapper.getScoreListByClassId(classId, semesterId);
        if (classScores.isEmpty()) {
            return MarkdownFormatter.noData("成绩");
        }
        String[] headers = {"学生", "学号", "课程", "总分"};
        List<String[]> rows = classScores.stream()
                .map(s -> new String[]{
                        s.getStudentName(),
                        s.getStudentNo(),
                        s.getCourseName(),
                        s.getTotalScore() != null ? String.valueOf(s.getTotalScore()) : "--"
                })
                .collect(Collectors.toList());
        return MarkdownFormatter.header("班级成绩列表") + "\n\n" + MarkdownFormatter.toTable(headers, rows);
    }

    @Tool(description = "获取近7天借阅趋势。当用户询问借阅趋势、借阅情况时调用此工具。")
    public String getBorrowTrend() {
        log.info("Tool调用: getBorrowTrend");
        List<Map<String, Object>> trend = dashboardMapper.borrowTrend7Days();
        if (trend.isEmpty()) {
            return MarkdownFormatter.noData("借阅趋势");
        }
        String[] headers = {"日期", "借阅量"};
        List<String[]> rows = trend.stream()
                .map(m -> new String[]{
                        String.valueOf(m.get("name")),
                        String.valueOf(m.get("value"))
                })
                .collect(Collectors.toList());
        return MarkdownFormatter.header("近7天借阅趋势") + "\n\n" + MarkdownFormatter.toTable(headers, rows);
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
        return MarkdownFormatter.header("座位使用率统计") + "\n\n"
                + MarkdownFormatter.toKeyValueList(
                        "座位总数", String.valueOf(totalSeats),
                        "使用中", String.valueOf(inUseReservations.size()),
                        "暂离", String.valueOf(awayReservations.size()),
                        "总占用", String.valueOf(usedSeats),
                        "使用率", String.format("%.1f%%", rate));
    }

    @Tool(description = "获取请假状态分布统计。当用户询问请假情况时调用此工具。")
    public String getLeaveStatistics() {
        log.info("Tool调用: getLeaveStatistics");
        List<Map<String, Object>> stats = dashboardMapper.leaveByStatus();
        if (stats.isEmpty()) {
            return MarkdownFormatter.noData("请假");
        }
        String[] headers = {"状态", "数量"};
        List<String[]> rows = stats.stream()
                .map(m -> new String[]{
                        String.valueOf(m.get("name")),
                        String.valueOf(m.get("value"))
                })
                .collect(Collectors.toList());
        return MarkdownFormatter.header("请假状态分布") + "\n\n" + MarkdownFormatter.toTable(headers, rows);
    }
}