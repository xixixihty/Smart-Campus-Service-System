package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.*;
import com.hxq.smart_campus.entity.vo.GpaVO;
import com.hxq.smart_campus.entity.vo.ScoreEntryDetailVO;
import com.hxq.smart_campus.entity.vo.ScoreEntryListVO;
import com.hxq.smart_campus.entity.vo.ScoreStatisticsVO;

import java.util.List;

public interface ScoreEntryService {
    /**
     * 插入成绩
     * @param scoreEntryCreateDTO
     * @return
     */
    ScoreEntryResponseDTO insertScore(ScoreEntryCreateDTO scoreEntryCreateDTO);
    /**
     * 更新成绩
     * @param scoreEntryUpdateDTO
     * @return
     */
    ScoreEntryResponseDTO updateScore(ScoreEntryUpdateDTO scoreEntryUpdateDTO);

    /**
     * 批量插入成绩
     * @param batchScoreEntryDTO
     * @return
     */
    boolean batchScore(BatchScoreEntryDTO batchScoreEntryDTO);

    /**
     * 获取成绩列表
     * @param pageNum
     * @param pageSize
     * @param courseId
     * @param studentId
     * @param semesterId
     * @return
     */
    PageInfo<ScoreEntryListVO> getScoreList(Integer pageNum, Integer pageSize, Long courseId, Long studentId, Long semesterId);

    /**
     * 获取成绩详情
     * @param id
     * @return
     */
    ScoreEntryDetailVO getScoreDetail(Long id);

    /**
     * 获取我的成绩列表
     * @param semesterId
     * @param courseId
     * @return
     */
    List<ScoreEntryListVO> getMyScoreList(Long semesterId, Long courseId);

    /**
     * 获取成绩统计信息
     * @param queryDTO
     * @return
     */
    ScoreStatisticsVO getScoreStatistics(ScoreStatisticsQueryDTO queryDTO);

    /**
     * 获取平均绩点
     * @param studentId
     * @param semesterId
     * @return
     */
    GpaVO getGpa(Long studentId, Long semesterId);
}
