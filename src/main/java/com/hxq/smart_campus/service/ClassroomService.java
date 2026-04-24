package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.ClassroomCreateDTO;
import com.hxq.smart_campus.entity.dto.ClassroomResponseDTO;
import com.hxq.smart_campus.entity.dto.ClassroomUpdateDTO;
import com.hxq.smart_campus.entity.vo.ClassroomDetailVO;
import com.hxq.smart_campus.entity.vo.ClassroomListVO;

import java.util.List;

public interface ClassroomService {
    /**
     * 获取教室列表
     *
     * @param pageNum
     * @param pageSize
     * @param building
     * @param type
     * @param status
     * @return
     */
    PageInfo<ClassroomListVO> getClassroomList(Integer pageNum, Integer pageSize, String building, String type, String status);

    /**
     * 获取教室详情
     * @param id
     * @return
     */
    ClassroomDetailVO getClassroomDetail(Long id);
    /**
     * 删除教室
     * @param ids
     * @return
     */
    boolean deleteClassroom(List<Long> ids);
    /**
     * 新增教室
     * @param classroomCreateDTO
     * @return
     */
    ClassroomResponseDTO insertClassroom(ClassroomCreateDTO classroomCreateDTO);
    /**
     * 更新教室
     * @param classroomUpdateDTO
     * @return
     */
    ClassroomResponseDTO updateClassroom(ClassroomUpdateDTO classroomUpdateDTO);
}