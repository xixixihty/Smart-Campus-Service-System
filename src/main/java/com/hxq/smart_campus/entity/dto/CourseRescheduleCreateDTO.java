package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class CourseRescheduleCreateDTO {
    private Long leaveRequestId;
    private String reason;
    private List<RescheduleItem> items;

    @Data
    public static class RescheduleItem {
        private Long courseScheduleId;
        private Integer newWeekDay;
        private Integer newStartSection;
        private Integer newEndSection;
        private String newWeekRange;
        private Long newClassroomId;
        private String reason;
    }
}