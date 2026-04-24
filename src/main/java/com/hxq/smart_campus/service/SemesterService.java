package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.SemesterCreateDTO;
import com.hxq.smart_campus.entity.dto.SemesterResponseDTO;
import com.hxq.smart_campus.entity.dto.SemesterUpdateDTO;
import com.hxq.smart_campus.entity.vo.SemesterDetailVO;
import com.hxq.smart_campus.entity.vo.SemesterListVO;

import java.util.List;

public interface SemesterService {
    /**
     *  分页查询学期列表
     * @param pageNum
     * @param pageSize
     * @param name
     * @param status
     * @return
     */
    PageInfo<SemesterListVO> getSemesterList(Integer pageNum, Integer pageSize, String name, String status);
    /**
     *  查询学期详情
     * @param id
     * @return
     */
    SemesterDetailVO getSemesterDetail(Long id);
    /**
     *  新增学期
     * @param semesterCreateDTO
     * @return
     */
    SemesterResponseDTO insertSemester(SemesterCreateDTO semesterCreateDTO);
    /**
     *  更新学期
     * @param semesterUpdateDTO
     * @return
     */
    SemesterResponseDTO updateSemester(SemesterUpdateDTO semesterUpdateDTO);
    /**
     *  批量删除学期
     * @param ids
     * @return
     */
    boolean deleteSemester(List<Long> ids);
    /**
     *  查询当前学期
     * @return
     */
    SemesterDetailVO getCurrentSemester();
    /**
     *  设置当前学期
     * @param id
     * @return
     */
    boolean setCurrentSemester(Long id);
}
