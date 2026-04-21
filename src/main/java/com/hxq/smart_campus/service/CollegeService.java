package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.CollegeCreateDTO;
import com.hxq.smart_campus.entity.dto.CollegeResponseDTO;
import com.hxq.smart_campus.entity.dto.CollegeUpdateDTO;
import com.hxq.smart_campus.entity.vo.CollegeDetailVO;
import com.hxq.smart_campus.entity.vo.CollegeListVO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CollegeService {
    /**
     * 获取学院列表
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @param collegeName 学院名称
     * @param status 状态
     * @return 学院列表
     */
    PageInfo<CollegeListVO> getCollegeList(Integer pageNum, Integer pageSize, String collegeName, String status);


    /**
     * 新增学院
     *
     * @param collegeCreateDTO 新增学院参数
     * @return 新增学院响应
     */
    CollegeResponseDTO createCollege(CollegeCreateDTO collegeCreateDTO);
    /**
     * 更新学院
     *
     * @param collegeUpdateDTO 更新学院参数
     * @return 更新学院响应
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
     * 更新学院状态
     *
     * @param id 学院ID
     * @return 更新结果
     */
    boolean updateStatus(Long id);
    /**
     * 获取学院详情
     *
     * @param id 学院ID
     * @return 学院详情
     */
    CollegeDetailVO getCollegeDetail(Long id);
}
