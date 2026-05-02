package com.hxq.smart_campus.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchScoreEntryDTO {
    private Long courseId;
    private Long semesterId;
    private List<ScoreEntryCreateDTO> scoreEntries; // 成绩列表，必填
}
