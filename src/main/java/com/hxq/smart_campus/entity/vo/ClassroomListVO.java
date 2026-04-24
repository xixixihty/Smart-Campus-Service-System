package com.hxq.smart_campus.entity.vo;

import lombok.Data;
@Data
public class ClassroomListVO {
    private Long id;                // 教室ID
    private String building;        // 教学楼
    private String roomNumber;      // 教室门牌号
    private Integer capacity;       // 座位容量
    private String type;            // 类型
    private String status;          // 状态
    private String createTime;      // 创建时间（格式化显示）
}
