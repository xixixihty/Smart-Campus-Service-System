package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.ClassCreateDTO;
import com.hxq.smart_campus.entity.dto.ClassResponseDTO;
import com.hxq.smart_campus.entity.dto.ClassUpdateDTO;
import com.hxq.smart_campus.entity.vo.ClassDetailVO;
import com.hxq.smart_campus.entity.vo.ClassListVO;

import java.util.List;

public interface ClassService {
    /**
     * 查询班级列表
     *
     * @param pageNum
     * @param pageSize
     * @param majorId
     * @param className
     * @param status
     * @return
     */
    PageInfo<ClassListVO> getClassList(Integer pageNum, Integer pageSize, Long majorId, String className, String status);
    /**
     * 新增班级
     *
     * @param classCreateDTO
     * @return
     */
    ClassResponseDTO insertClass(ClassCreateDTO classCreateDTO);
    /**
     * 查询班级详情
     *
     * @param id
     * @return
     */
    ClassDetailVO getClassDetail(Long id);
    /**
     * 更新班级
     *
     * @param classUpdateDTO
     * @return
     */
    ClassResponseDTO updateClass(ClassUpdateDTO classUpdateDTO);
    /**
     * 删除班级
     *
     * @param ids
     * @return
     */
    boolean deleteClass(List<Long> ids);
    /**
     * 更新班级状态
     *
     * @param id
     * @return
     */
    boolean updateStatus(Long id);
}
