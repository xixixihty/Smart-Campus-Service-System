package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.CollegeCreateDTO;
import com.hxq.smart_campus.entity.dto.CollegeResponseDTO;
import com.hxq.smart_campus.entity.dto.CollegeUpdateDTO;
import com.hxq.smart_campus.entity.vo.CollegeDetailVO;
import com.hxq.smart_campus.entity.vo.CollegeListVO;

import java.util.List;

/**
 * 学院管理服务接口
 *
 * @author XiongQi He
 * @since 2026-04-21
 */
public interface CollegeService {

    /**
     * 获取学院列表
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param collegeName 学院名称
     * @param status 状态
     * @return 学院列表
     */
    PageInfo<CollegeListVO> getCollegeList(Integer pageNum, Integer pageSize, String collegeName, String status);

    /**
     * 新增学院
     *
     * @param collegeCreateDTO 学院创建DTO
     * @return 学院响应DTO
     */
    CollegeResponseDTO createCollege(CollegeCreateDTO collegeCreateDTO);

    /**
     * 更新学院
     *
     * @param collegeUpdateDTO 学院更新DTO
     * @return 学院响应DTO
     */
    CollegeResponseDTO updateCollege(CollegeUpdateDTO collegeUpdateDTO);

    /**
     * 删除学院
     *
     * @param ids 学院ID列表
     * @return 删除结果
     */
    boolean deleteCollege(List<Long> ids);

    /**
     * 获取学院详情
     *
     * @param id 学院ID
     * @return 学院详情
     */
    CollegeDetailVO getCollegeDetail(Long id);
    /**
     * 更新学院状态
     *
     * @param id 学院ID
     * @return 更新结果
     */
    boolean updateStatus(Long id);
}