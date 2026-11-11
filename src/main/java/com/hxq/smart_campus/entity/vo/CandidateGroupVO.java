package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * 按课程分组的补考候选学生
 */
@Data
public class CandidateGroupVO {
    private Long courseId;
    private String courseName;
    private String courseCode;
    private Integer candidateCount;
    private List<MakeupExamCandidateVO> candidates;
}