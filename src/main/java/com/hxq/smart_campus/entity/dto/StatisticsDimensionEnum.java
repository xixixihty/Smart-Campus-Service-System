package com.hxq.smart_campus.entity.dto;

/**
 * 成绩统计维度枚举
 * 用于指定成绩统计分析的维度类型
 */
public enum StatisticsDimensionEnum {
    /** 个人维度：按学生个人统计 */
    STUDENT(0, "个人维度"),
    /** 课程维度：按单科课程统计 */
    COURSE(1, "课程维度"),
    /** 班级维度：按班级统计（包含班级所有学生） */
    CLASS(2, "班级维度"),
    /** 专业维度：按专业统计（包含该专业下所有班级） */
    MAJOR(3, "专业维度"),
    /** 学院维度：按学院统计（包含该学院下所有专业） */
    COLLEGE(4, "学院维度");

    private final Integer code;
    private final String description;

    StatisticsDimensionEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
