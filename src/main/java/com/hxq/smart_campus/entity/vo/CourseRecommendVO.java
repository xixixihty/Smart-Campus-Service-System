package com.hxq.smart_campus.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRecommendVO {
    private Long id;
    private String courseName;
    private String courseType;
    private BigDecimal credit;
    private String teacherName;
    private String reason;
    private Integer rating;
}