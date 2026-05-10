package com.hxq.ai.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 课程信息表
 * </p>
 *
 * @author hexiongqi
 * @since 2026-04-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("course")
public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程方向
     */
    private String direction;

    /**
     * 课程简介
     */
    private String description;

    /**
     * 目标薪资范围
     */
    private String targetSalary;

    /**
     * 是否开放试听
     */
    private Integer isActive;

    private LocalDateTime createdAt;


}
