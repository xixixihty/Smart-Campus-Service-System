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
     * @param 课程不存在
     * @return
     */
    public static CourseSelectionException courseNotExist(String 课程不存在) {
        return new CourseSelectionException("COURSE_NOT_EXIST", "课程不存在: " + 课程不存在);
    }
    /**
     * 课程选修过
     * @param 已经选修过该课程
     * @return
     */
    public static CourseSelectionException courseSelectionExist(String 已经选修过该课程) {
        return new CourseSelectionException("COURSE_SELECTION_EXIST", "已经选修过该课程: " + 已经选修过该课程);
    }

    /**
     * 课程已满
     * @param 课程已满
     * @return
     */
    public static CourseSelectionException courseSelectionFull(String 课程已满) {
        return new CourseSelectionException("COURSE_SELECTION_FULL", "课程已满: " + 课程已满);
    }

    /**
     * 课程选课失败
     * @param s
     * @return
     */
    public static CourseSelectionException insertCourseSelectionFailed(String s) {
        return new CourseSelectionException("COURSE_SELECTION_FAILED", "课程选课失败: " + s);
    }

    /**
     * 选课时间不在范围内
     * @param 选课时间不在范围内
     * @return
     */
    public static CourseSelectionException courseSelectionTimePeriodInvalid(String 选课时间不在范围内) {
        return new CourseSelectionException("COURSE_SELECTION_TIME_PERIOD_INVALID", "选课时间不在范围内: " + 选课时间不在范围内);
    }

    /**
     * 可选课程列表为空
     * @param 可选课程列表为空
     * @return
     */
    public static CourseSelectionException availableCourseListEmpty(String 可选课程列表为空) {
        return new CourseSelectionException("AVAILABLE_COURSE_LIST_EMPTY", "可选课程列表为空: " + 可选课程列表为空);
    }

    /**
     *
     * @param 课程不存在
     * @return
     */
    public static CourseSelectionException courseSelectionNotExist(String 课程不存在) {
        return new CourseSelectionException("COURSE_SELECTION_NOT_EXIST", "课程不存在: " + 课程不存在);
    }
    /**
     * 退课失败
     * @param s
     * @return
     */
    public static CourseSelectionException dropCourseFailed(String s) {
        return new CourseSelectionException("DROP_COURSE_FAILED", "退课失败: " + s);
    }

    /**
     * 学生不存在
     * @param 学生不存在
     * @return
     */
    public static CourseSelectionException studentNotExist(String 学生不存在) {
        return new CourseSelectionException("STUDENT_NOT_EXIST", "学生不存在: " + 学生不存在);
    }

    /**
     * 选课时间冲突
     * @param 选课时间冲突
     * @return
     */
    public static CourseSelectionException timeConflict(String 选课时间冲突) {
        return new CourseSelectionException("TIME_CONFLICT", "选课时间冲突: " + 选课时间冲突);
    }
}
