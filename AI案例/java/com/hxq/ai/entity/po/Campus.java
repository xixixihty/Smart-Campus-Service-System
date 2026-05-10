package com.hxq.ai.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 校区信息表
 * </p>
 *
 * @author hexiongqi
 * @since 2026-04-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("campus")
public class Campus implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 校区ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 校区名称
     */
    private String campusName;

    /**
     * 所属城市
     */
    private String city;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 校区电话
     */
    private String contactPhone;

    /**
     * 是否运营中
     */
    private Integer isActive;


}
