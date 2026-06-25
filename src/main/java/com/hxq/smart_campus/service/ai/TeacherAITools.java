package com.hxq.smart_campus.service.ai;

import com.hxq.smart_campus.entity.vo.*;
import com.hxq.smart_campus.mapper.*;
import com.hxq.smart_campus.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 教师端 AI 工具集 —— 提供教师专属的数据查询能力，供 AI 模型调用
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TeacherAITools {

    private final TeacherDashboardMapper teacherDashboardMapper;
    private final ScoreEntryMapper scoreEntryMapper;
    private final LeaveApprovalMapper leaveApprovalMapper;
    private final CourseRescheduleMapper courseRescheduleMapper;

    @Tool(description = "获取当前教师的个人课表。当教师询问课表、上课时间、课程安排等问题时调用此工具。")
    public String getMyTeachingSchedule() {
        Long teacherId = SecurityUtils.getCurrentUserId();
        log.info("Tool调用: getMyTeachingSchedule, teacherId={}", teacherId);
        List<CourseRescheduleVO> schedules = courseRescheduleMapper.getTeacherCourseSchedules(teacherId);
        if (schedules.isEmpty()) {
            return MarkdownFormatter.noData("课表");
        }
        String[] headers = {"课程", "星期", "节次", "教室", "周次"};
        List<String[]> rows = schedules.stream()
                .map(s -> new String[]{
                        s.getCourseName(),
                        "星期" + s.getOriginalWeekDay(),
                        "第" + s.getOriginalStartSection() + "-" + s.getOriginalEndSection() + "节",
                        s.getOriginalClassroomName() != null ? s.getOriginalClassroomName() : "未安排",
                        s.getOriginalWeekRange() != null ? s.getOriginalWeekRange() : "全学期"
                })
                .collect(Collectors.toList());
        return MarkdownFormatter.header("我的课表") + "\n\n" + MarkdownFormatter.toTable(headers, rows);
    }

    @Tool(description = "获取当前教师的教学统计数据。当教师询问授课班级数、学生数、待批请假数等统计信息时调用此工具。")
    public String getMyTeachingStats() {
        Long teacherId = SecurityUtils.getCurrentUserId();
        log.info("Tool调用: getMyTeachingStats, teacherId={}", teacherId);
        Integer courseCount = teacherDashboardMapper.getTeachingCourseCount(teacherId, null);
        Integer classCount = teacherDashboardMapper.getTeachingClassCount(teacherId, null);
        Integer studentCount = teacherDashboardMapper.getTeachingStudentCount(teacherId, null);
        Integer pendingLeave = teacherDashboardMapper.getPendingLeaveCount(teacherId);
        Integer pendingReschedule = teacherDashboardMapper.getPendingRescheduleCount(teacherId);

        return MarkdownFormatter.header("教学数据统计") + "\n\n"
                + MarkdownFormatter.toKeyValueList(
                        "所授课程数", String.valueOf(courseCount != null ? courseCount : 0),
                        "授课班级数", String.valueOf(classCount != null ? classCount : 0),
                        "授课学生数", String.valueOf(studentCount != null ? studentCount : 0),
                        "待批请假数", String.valueOf(pendingLeave != null ? pendingLeave : 0),
                        "待处理调课数", String.valueOf(pendingReschedule != null ? pendingReschedule : 0));
    }

    @Tool(description = "获取当前教师的授课班级列表。当教师询问班级信息、授课班级时调用此工具。")
    public String getMyTeachingClasses() {
        Long teacherId = SecurityUtils.getCurrentUserId();
        log.info("Tool调用: getMyTeachingClasses, teacherId={}", teacherId);
        List<ClassStudentCountVO> classes = teacherDashboardMapper.getClassStudentDistribution(teacherId, null);
        if (classes.isEmpty()) {
            return MarkdownFormatter.noData("授课班级");
        }
        String[] headers = {"班级", "学生数"};
        List<String[]> rows = classes.stream()
                .map(c -> new String[]{
                        c.getClassName(),
                        String.valueOf(c.getStudentCount())
                })
                .collect(Collectors.toList());
        return MarkdownFormatter.header("授课班级列表") + "\n\n" + MarkdownFormatter.toTable(headers, rows);
    }

    @Tool(description = "获取待审批的请假申请列表。当教师询问待批请假、请假审批时调用此工具。")
    public String getPendingLeaveApprovals() {
        Long teacherId = SecurityUtils.getCurrentUserId();
        log.info("Tool调用: getPendingLeaveApprovals, teacherId={}", teacherId);
        List<PendingLeaveRequestVO> list = leaveApprovalMapper.getPendingApprovalList("待审批", teacherId);
        if (list.isEmpty()) {
            return MarkdownFormatter.noData("待审批请假");
        }
        String[] headers = {"学生", "请假类型", "开始时间", "结束时间", "原因"};
        List<String[]> rows = list.stream()
                .map(l -> new String[]{
                        l.getStudentName(),
                        l.getLeaveType() != null ? l.getLeaveType() : "--",
                        l.getStartTime() != null ? l.getStartTime().toString() : "--",
                        l.getEndTime() != null ? l.getEndTime().toString() : "--",
                        l.getReason() != null ? l.getReason() : "--"
                })
                .collect(Collectors.toList());
        return MarkdownFormatter.header("待审批请假申请）") + "\n\n" + MarkdownFormatter.toTable(headers, rows);
    }

    @Tool(description = "获取当前教师的调课记录。当教师询问调课状态、调课历史时调用此工具。")
    public String getMyRescheduleRecords() {
        Long teacherId = SecurityUtils.getCurrentUserId();
        log.info("Tool调用: getMyRescheduleRecords, teacherId={}", teacherId);
        List<CourseRescheduleVO> records = courseRescheduleMapper.getRescheduleListByTeacher(teacherId);
        if (records.isEmpty()) {
            return MarkdownFormatter.noData("调课记录");
        }
        String[] headers = {"课程", "原时间", "新时间", "状态", "原因"};
        List<String[]> rows = records.stream()
                .map(r -> new String[]{
                        r.getCourseName(),
                        "星期" + r.getOriginalWeekDay() + " 第" + r.getOriginalStartSection() + "-" + r.getOriginalEndSection() + "节",
                        "星期" + r.getNewWeekDay() + " 第" + r.getNewStartSection() + "-" + r.getNewEndSection() + "节",
                        r.getStatus(),
                        r.getReason() != null ? r.getReason() : "--"
                })
                .collect(Collectors.toList());
        return MarkdownFormatter.header("调课记录") + "\n\n" + MarkdownFormatter.toTable(headers, rows);
    }

    @Tool(description = "获取指定班级的学生成绩列表。当教师询问班级成绩、学生成绩分布时调用此工具。参数classId为班级ID。")
    public String getClassScores(@ToolParam(description = "班级ID") Long classId) {
        log.info("Tool调用: getClassScores, classId={}", classId);
        List<ScoreEntryListVO> scores = scoreEntryMapper.getScoreListByClassId(classId, null);
        if (scores.isEmpty()) {
            return MarkdownFormatter.noData("成绩");
        }
        String[] headers = {"学生", "课程", "平时分", "期末分", "总分"};
        List<String[]> rows = scores.stream()
                .map(s -> new String[]{
                        s.getStudentName(),
                        s.getCourseName(),
                        s.getUsualScore() != null ? String.valueOf(s.getUsualScore()) : "--",
                        s.getFinalScore() != null ? String.valueOf(s.getFinalScore()) : "--",
                        s.getTotalScore() != null ? String.valueOf(s.getTotalScore()) : "--"
                })
                .collect(Collectors.toList());
        return MarkdownFormatter.header("班级成绩列表") + "\n\n" + MarkdownFormatter.toTable(headers, rows);
    }
}