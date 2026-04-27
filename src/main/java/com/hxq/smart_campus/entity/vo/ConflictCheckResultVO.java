package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class ConflictCheckResultVO {
    private Boolean hasConflict;            // 是否存在冲突
    private List<ConflictDetailVO> conflicts; // 冲突详情列表
    private String message;                 // 冲突提示信息

    @Data
    public static class ConflictDetailVO {
        private String conflictType;        // 冲突类型：TEACHER/CLASSROOM/CLASS
        private String conflictReason;      // 冲突原因
        private CourseScheduleDetailVO conflictSchedule; // 冲突的排课信息
    }
}