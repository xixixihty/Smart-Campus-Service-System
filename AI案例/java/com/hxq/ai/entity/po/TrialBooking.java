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
 * 试听预约记录表
 * </p>
 *
 * @author hexiongqi
 * @since 2026-04-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("trial_booking")
public class TrialBooking implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 预约单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学员姓名
     */
    private String studentName;

    /**
     * 学员手机号
     */
    private String studentPhone;

    /**
     * 学员微信号
     */
    private String studentWechat;

    /**
     * 学员所在城市
     */
    private String studentCity;

    /**
     * 试听课程ID
     */
    private Integer courseId;

    /**
     * 试听校区ID
     */
    private Integer campusId;

    /**
     * 预约时间
     */
    private LocalDateTime bookingTime;

    private String status;

    /**
     * 备注
     */
    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
