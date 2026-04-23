package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.MajorCreateDTO;
import com.hxq.smart_campus.entity.dto.MajorResponseDTO;
import com.hxq.smart_campus.entity.dto.MajorUpdateDTO;
import com.hxq.smart_campus.entity.vo.MajorDetailVO;
import com.hxq.smart_campus.entity.vo.MajorListVO;

import java.util.List;

public interface MajorService {
    /**
     * 查询专业列表
     * @param pageNum
     * @param pageSize
     * @param collegeId
     * @param majorName
     * @param status
     * @return
     */
    PageInfo<MajorListVO> getMajorList(Integer pageNum, Integer pageSize, Long collegeId, String majorName, String status);
    /**
     * 新增专业
     * @param majorCreateDTO
     * @return
     */
    MajorResponseDTO insertMajor(MajorCreateDTO majorCreateDTO);
    /**
     * 更新专业
     * @param majorUpdateDTO
     * @return
     */
    MajorResponseDTO updateMajor(MajorUpdateDTO majorUpdateDTO);
    /**
     * 批量删除专业
     * @param ids 专业ID列表
     * @return
     */
    boolean deleteMajorByIds(List<Long> ids);
    /**
     * 查询专业详情
     * @param id 专业ID
     * @return
     */
    MajorDetailVO getMajorDetail(Long id);
    /**
     * 更新专业状态
     * @param id 专业ID
     * @return
     */
    boolean updateMajorStatus(Long id);
}
