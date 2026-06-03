package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.pojo.ScoreAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 成绩审计日志 Mapper
 */
@Mapper
public interface ScoreAuditLogMapper {

    int insert(ScoreAuditLog log);
}