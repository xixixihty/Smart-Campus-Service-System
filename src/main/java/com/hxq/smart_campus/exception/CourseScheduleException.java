package com.hxq.smart_campus.exception;

public class CourseScheduleException extends BusinessException {
    public CourseScheduleException(String errorMessage) {
        super("COURSE_SCHEDULE_ERROR", errorMessage);
    }

    public CourseScheduleException(String errorMessage, Throwable cause) {
        super("COURSE_SCHEDULE_ERROR", errorMessage, cause);
    }

    public static CourseScheduleException conflict(String conflictReason) {
        return new CourseScheduleException("排课冲突: " + conflictReason);
    }

    public static CourseScheduleException notFound(Long id) {
        return new CourseScheduleException("排课不存在: id=" + id);
    }

    public static CourseScheduleException createFailed(String reason) {
        return new CourseScheduleException("创建排课失败: " + reason);
    }

    public static CourseScheduleException updateFailed(String reason) {
        return new CourseScheduleException("更新排课失败: " + reason);
    }

    public static CourseScheduleException deleteFailed(String reason) {
        return new CourseScheduleException("删除排课失败: " + reason);
    }
}