package com.hxq.smart_campus.service.ai;

import com.hxq.smart_campus.entity.vo.*;
import com.hxq.smart_campus.mapper.*;
import com.hxq.smart_campus.utils.SecurityUtils;
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
            return "当前没有排课记录。";
        }
        return schedules.stream()
                .map(s -> String.format("课程：%s | 星期%d | 第%d-%d节 | 教室：%s | 周次：%s",
                        s.getCourseName(),
                        s.getOriginalWeekDay(),
                        s.getOriginalStartSection(),
                        s.getOriginalEndSection(),
                        s.getOriginalClassroomName() != null ? s.getOriginalClassroomName() : "未安排",
                        s.getOriginalWeekRange() != null ? s.getOriginalWeekRange() : "全学期"))
                .collect(Collectors.joining("\n"));
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

        return String.format(
                "教学数据统计：\n" +
                "- 所授课程数：%d\n" +
                "- 授课班级数：%d\n" +
                "- 授课学生数：%d\n" +
                "- 待批请假数：%d\n" +
                "- 待处理调课数：%d",
                courseCount != null ? courseCount : 0,
                classCount != null ? classCount : 0,
                studentCount != null ? studentCount : 0,
                pendingLeave != null ? pendingLeave : 0,
                pendingReschedule != null ? pendingReschedule : 0);
    }

    @Tool(description = "获取当前教师的授课班级列表。当教师询问班级信息、授课班级时调用此工具。")
    public String getMyTeachingClasses() {
        Long teacherId = SecurityUtils.getCurrentUserId();
        log.info("Tool调用: getMyTeachingClasses, teacherId={}", teacherId);
        List<ClassStudentCountVO> classes = teacherDashboardMapper.getClassStudentDistribution(teacherId, null);
        if (classes.isEmpty()) {
            return "当前没有授课班级。";
        }
        return classes.stream()
                .map(c -> String.format("班级：%s | 学生数：%d",
                        c.getClassName(), c.getStudentCount()))
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "获取待审批的请假申请列表。当教师询问待批请假、请假审批时调用此工具。")
    public String getPendingLeaveApprovals() {
        Long teacherId = SecurityUtils.getCurrentUserId();
        log.info("Tool调用: getPendingLeaveApprovals, teacherId={}", teacherId);
        List<PendingLeaveRequestVO> list = leaveApprovalMapper.getPendingApprovalList("待审批", teacherId);
        if (list.isEmpty()) {
            return "当前没有待审批的请假申请。";
        }
        return list.stream()
                .map(l -> String.format("学生：%s | 请假类型：%s | 时间：%s ~ %s | 原因：%s",
                        l.getStudentName(),
                        l.getLeaveType() != null ? l.getLeaveType() : "--",
                        l.getStartTime() != null ? l.getStartTime() : "--",
                        l.getEndTime() != null ? l.getEndTime() : "--",
                        l.getReason() != null ? l.getReason() : "--"))
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "获取当前教师的调课记录。当教师询问调课状态、调课历史时调用此工具。")
    public String getMyRescheduleRecords() {
        Long teacherId = SecurityUtils.getCurrentUserId();
        log.info("Tool调用: getMyRescheduleRecords, teacherId={}", teacherId);
        List<CourseRescheduleVO> records = courseRescheduleMapper.getRescheduleListByTeacher(teacherId);
        if (records.isEmpty()) {
            return "当前没有调课记录。";
        }
        return records.stream()
                .map(r -> String.format("课程：%s | 原：星期%d 第%d-%d节 | 新：星期%d 第%d-%d节 | 状态：%s | 原因：%s",
                        r.getCourseName(),
                        r.getOriginalWeekDay(), r.getOriginalStartSection(), r.getOriginalEndSection(),
                        r.getNewWeekDay(), r.getNewStartSection(), r.getNewEndSection(),
                        r.getStatus(),
                        r.getReason() != null ? r.getReason() : "--"))
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "获取指定班级的学生成绩列表。当教师询问班级成绩、学生成绩分布时调用此工具。参数classId为班级ID。")
    public String getClassScores(@ToolParam(description = "班级ID") Long classId) {
        log.info("Tool调用: getClassScores, classId={}", classId);
        List<ScoreEntryListVO> scores = scoreEntryMapper.getScoreListByClassId(classId, null);
        if (scores.isEmpty()) {
            return "该班级暂无成绩记录。";
        }
        return scores.stream()
                .map(s -> String.format("学生：%s | 课程：%s | 平时分：%s | 期末分：%s | 总分：%s",
                        s.getStudentName(),
                        s.getCourseName(),
                        s.getUsualScore() != null ? s.getUsualScore() : "--",
                        s.getFinalScore() != null ? s.getFinalScore() : "--",
                        s.getTotalScore() != null ? s.getTotalScore() : "--"))
                .collect(Collectors.joining("\n"));
    }
}