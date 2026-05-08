package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.ClassroomCreateDTO;
import com.hxq.smart_campus.entity.dto.ClassroomResponseDTO;
import com.hxq.smart_campus.entity.dto.ClassroomUpdateDTO;
import com.hxq.smart_campus.entity.vo.ClassroomDetailVO;
import com.hxq.smart_campus.entity.vo.ClassroomListVO;
import com.hxq.smart_campus.mapper.ClassroomMapper;
import com.hxq.smart_campus.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.hxq.smart_campus.constant.MessageConstant.CLASSROOM_STATUS_DISABLED;
import static com.hxq.smart_campus.constant.MessageConstant.DATE_TIME_FORMATTER;

@RequiredArgsConstructor
@Slf4j
@Service
public class ClassroomServiceImpl implements ClassroomService {
    private final ClassroomMapper classroomMapper;

    /**
     * 获取教室列表
     * @param pageNum
     * @param pageSize
     * @param building
     * @param type
     * @param status
     * @return
     */
    @Override
    public PageInfo<ClassroomListVO> getClassroomList(Integer pageNum, Integer pageSize, String building, String type, String status) {
        PageHelper.startPage(pageNum, pageSize);
        List<ClassroomListVO> list = classroomMapper.selectByBuildingAndTypeAndStatus(building, type, status);
        return new PageInfo<>(list);
    }
    /**
     * 获取教室详情
     * @param id
     * @return
     */
    @Override
    public ClassroomDetailVO getClassroomDetail(Long id) {
        return classroomMapper.getClassroomDetail(id);
    }
    /**
     * 删除教室
     * @param ids
     * @return
     */
    @Override
    public boolean deleteClassroom(List<Long> ids) {
        // 删除教室
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("教室ID不能为空");
        }
        for (Long id : ids) {
            // 检查状态：只有停用的教室才能删除
            ClassroomDetailVO classroom = classroomMapper.getClassroomDetail(id);
            if (classroom == null) {
                throw new IllegalArgumentException("教室不存在, id=" + id);
            }
            if (!CLASSROOM_STATUS_DISABLED.equals(classroom.getStatus())) {
                throw new IllegalArgumentException("只有停用的教室才能删除，当前状态: " + classroom.getStatus());
            }
        }
        int result = classroomMapper.deleteBatch(ids);
        if (result <= 0) {
            throw new RuntimeException("删除失败");
        }
        return true;
    }

    /**
     * 新增教室
     * @param classroomCreateDTO
     * @return
     */
    @Override
    public ClassroomResponseDTO insertClassroom(ClassroomCreateDTO classroomCreateDTO) {
        if (classroomCreateDTO == null || classroomCreateDTO.getBuilding() == null || classroomCreateDTO.getRoomNumber() == null) {
            throw new IllegalArgumentException("教室信息不能为空");
        }
        int result = classroomMapper.insertClassroom(classroomCreateDTO);
        if (result <= 0) {
            throw new RuntimeException("新增失败");
        }
        // 获取最新插入的教室ID
        Long id = classroomMapper.getLastInsertId();
        return convertToClassroomResponseDTO(classroomMapper.getClassroomDetail(id));
    }
    /**
     * 更新教室
     * @param classroomUpdateDTO
     * @return
     */
    @Override
    public ClassroomResponseDTO updateClassroom(ClassroomUpdateDTO classroomUpdateDTO) {
        if (classroomUpdateDTO == null || classroomUpdateDTO.getId() == null) {
            throw new IllegalArgumentException("教室信息不能为空");
        }
        int result = classroomMapper.updateClassroom(classroomUpdateDTO);
        if (result <= 0) {
            throw new RuntimeException("更新失败");
        }
        return convertToClassroomResponseDTO(classroomMapper.getClassroomDetail(classroomUpdateDTO.getId()));
    }
    /**
     * 将教室详情VO转换为教室响应DTO
     * @param classroomDetailVO
     * @return
     */
    private ClassroomResponseDTO convertToClassroomResponseDTO(ClassroomDetailVO classroomDetailVO) {
        if (classroomDetailVO == null) {
            return null;
        }
        ClassroomResponseDTO responseDTO = new ClassroomResponseDTO();
        responseDTO.setId(classroomDetailVO.getId());
        responseDTO.setBuilding(classroomDetailVO.getBuilding());
        responseDTO.setRoomNumber(classroomDetailVO.getRoomNumber());
        responseDTO.setCapacity(classroomDetailVO.getCapacity());
        responseDTO.setType(classroomDetailVO.getType());
        responseDTO.setStatus(classroomDetailVO.getStatus());
        try {
            if (classroomDetailVO.getCreateTime() != null && !classroomDetailVO.getCreateTime().isEmpty()) {
                responseDTO.setCreateTime(LocalDateTime.parse(classroomDetailVO.getCreateTime(), DATE_TIME_FORMATTER));
            }
            if (classroomDetailVO.getUpdateTime() != null && !classroomDetailVO.getUpdateTime().isEmpty()) {
                responseDTO.setUpdateTime(LocalDateTime.parse(classroomDetailVO.getUpdateTime(), DATE_TIME_FORMATTER));
            }
        } catch (Exception e) {
            log.error("日期转换错误", e);
        }
        return responseDTO;
    }
}
