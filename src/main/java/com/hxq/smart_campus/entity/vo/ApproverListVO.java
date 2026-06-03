package com.hxq.smart_campus.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class ApproverListVO {
    private Long defaultApproverId;
    private List<ApproverVO> approvers;
}