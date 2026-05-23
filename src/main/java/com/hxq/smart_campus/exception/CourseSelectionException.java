package com.hxq.smart_campus.exception;

import lombok.Getter;

/**
 * 课程选课异常
 */
@Getter
public class CourseSelectionException extends BusinessException {
    public CourseSelectionException(String errorMessage) {
        super(errorMessage);
    }

    public CourseSelectionException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public CourseSelectionException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);

    }

    /**
     * 课程不在选课时间段内
     * @param reason
     * @return
     */
    public static CourseSelectionException outOfTimePeriod(String reason) {
        return new CourseSelectionException("TIME_PERIOD_ERROR", "不在选课时间段内: " + reason);
    }

    /**
     *
     * @param reason
     * @return
     */
    public static CourseSelectionException courseNotExist(String reason) {
        return new CourseSelectionException("COURSE_NOT_EXIST", "课程不存在: " + reason);
    }
    /**
     * 课程选修过
     * @param reason
     * @return
     */
    public static CourseSelectionException courseSelectionExist(String reason) {
        return new CourseSelectionException("COURSE_SELECTION_EXIST", "已经选修过该课程: " + reason);
    }

    /**
     * 课程已满
     * @param reason
     * @return
     */
    public static CourseSelectionException courseSelectionFull(String reason) {
        return new CourseSelectionException("COURSE_SELECTION_FULL", "课程已满: " + reason);
    }

    /**
     * 课程选课失败
     * @param reason
     * @return
     */
    public static CourseSelectionException insertCourseSelectionFailed(String reason) {
        return new CourseSelectionException("COURSE_SELECTION_FAILED", "课程选课失败: " + reason);
    }

    /**
     * 选课时间不在范围内
     * @param reason
     * @return
     */
    public static CourseSelectionException courseSelectionTimePeriodInvalid(String reason) {
        return new CourseSelectionException("COURSE_SELECTION_TIME_PERIOD_INVALID", "选课时间不在范围内: " + reason);
    }

    /**
     * 可选课程列表为空
     * @param reason
     * @return
     */
    public static CourseSelectionException availableCourseListEmpty(String reason) {
        return new CourseSelectionException("AVAILABLE_COURSE_LIST_EMPTY", "可选课程列表为空: " + reason);
    }

    /**
     *
     * @param reason
     * @return
     */
    public static CourseSelectionException courseSelectionNotExist(String reason) {
        return new CourseSelectionException("COURSE_SELECTION_NOT_EXIST", "课程不存在: " + reason);
    }
    /**
     * 退课失败
     * @param reason
     * @return
     */
    public static CourseSelectionException dropCourseFailed(String reason) {
        return new CourseSelectionException("DROP_COURSE_FAILED", "退课失败: " + reason);
    }

    /**
     * 学生不存在
     * @param reason
     * @return
     */
    public static CourseSelectionException studentNotExist(String reason) {
        return new CourseSelectionException("STUDENT_NOT_EXIST", "学生不存在: " + reason);
    }

    /**
     * 选课时间冲突
     * @param reason
     * @return
     */
    public static CourseSelectionException timeConflict(String reason) {
        return new CourseSelectionException("TIME_CONFLICT", "选课时间冲突: " + reason);
    }
}
