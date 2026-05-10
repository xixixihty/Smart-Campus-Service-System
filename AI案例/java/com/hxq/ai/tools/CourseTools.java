package com.hxq.ai.tools;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.hxq.ai.entity.po.Campus;
import com.hxq.ai.entity.po.Course;
import com.hxq.ai.entity.po.TrialBooking;
import com.hxq.ai.entity.query.CourseQuery;
import com.hxq.ai.service.ICampusService;
import com.hxq.ai.service.ICourseService;
import com.hxq.ai.service.ITrialBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Component
public class CourseTools {

    private final ICourseService courseService;
    private final ICampusService campusService;
    private final ITrialBookingService trialBookingService;

    @Tool(description = "查询指定方向的课程信息，返回课程列表及详情")
    public List<Course> getCourseList(
            @ToolParam(description = "课程方向，如Java、Python、前端等") String direction,
            @ToolParam(description = "搜索关键词，可选") String keyword) {
        if (direction == null && keyword == null) {
            return courseService.list();
        }
        QueryChainWrapper<Course> wrapper = courseService.query()
                .eq(direction != null, "direction", direction)
                .like(keyword != null && !keyword.isEmpty(), "course_name", keyword);
        return wrapper.list();
    }
    @Tool(description = "查询所有校区")
    public List<Campus> getCampusList() {
        return campusService.list();
    }

    @Tool(description ="创建预约单，返回预约单id")
    public Integer createTrialBooking(
            @ToolParam(description ="学员姓名") String studentName,
            @ToolParam(description ="学员手机号") String studentPhone,
            @ToolParam(description ="试听课程id") Integer courseId,
            @ToolParam(description ="试听校区id") Integer campusId,
            @ToolParam(description ="预约时间") String reservationTime,
            @ToolParam(description ="学员微信号", required = false) String studentWechat,
            @ToolParam(description ="学员所在城市") String studentCity,
            @ToolParam(description ="备注" , required = false) String remark
    ) {
    TrialBooking trialBooking = new TrialBooking();
    trialBooking.setStudentName(studentName);
    trialBooking.setStudentPhone(studentPhone);
    trialBooking.setCourseId(courseId);
    trialBooking.setCampusId(campusId);
    trialBooking.setBookingTime(LocalDateTime.parse(reservationTime));
    trialBooking.setStudentWechat(studentWechat);
    trialBooking.setStudentCity(studentCity);
    trialBooking.setRemark(remark);
    trialBookingService.save(trialBooking);
    return trialBooking.getId();
    }
}
