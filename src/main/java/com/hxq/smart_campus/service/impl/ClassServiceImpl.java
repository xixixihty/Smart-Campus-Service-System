package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.constant.MessageConstant;
import com.hxq.smart_campus.entity.dto.ClassCreateDTO;
import com.hxq.smart_campus.entity.dto.ClassResponseDTO;
import com.hxq.smart_campus.entity.dto.ClassUpdateDTO;
import com.hxq.smart_campus.entity.vo.ClassDetailVO;
import com.hxq.smart_campus.entity.vo.ClassListVO;
import com.hxq.smart_campus.mapper.ClassMapper;
import com.hxq.smart_campus.service.ClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.hxq.smart_campus.constant.MessageConstant.CLASS_STATUS_GRADUATED;
import static com.hxq.smart_campus.constant.MessageConstant.DATE_TIME_FORMATTER;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final ClassMapper classMapper;
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
    @Override
    public PageInfo<ClassListVO> getClassList(Integer pageNum, Integer pageSize, Long majorId, String className, String status) {
        PageHelper.startPage(pageNum, pageSize);
        List<ClassListVO> classListVOList = classMapper.getClassList(majorId, className, status);
        return new PageInfo<>(classListVOList);
    }
    /**
     * 新增班级
     *
     * @param classCreateDTO
     * @return
     */
    @Override
    public ClassResponseDTO insertClass(ClassCreateDTO classCreateDTO) {
        // 参数校验
        if (classCreateDTO == null || classCreateDTO.getMajorId() == null || classCreateDTO.getClassName() == null) {
            throw new IllegalArgumentException("班级创建参数不能为空");
        }
        // 新增班级
        int result = classMapper.insertClass(classCreateDTO);
        if (result <= 0) {
            throw new RuntimeException("班级创建失败");
        }
        Long newClassId = classMapper.getLastInsertId();
        ClassDetailVO classDetailVO = classMapper.getClassDetail(newClassId);
        if (classDetailVO == null) {
            throw new RuntimeException("班级创建失败，未查询到班级详情");
        }
        return convertToClassResponseDTO(classDetailVO);
    }
    /**
     * 查询班级详情
     *
     * @param id
     * @return
     */
    @Override
    public ClassDetailVO getClassDetail(Long id) {
        return classMapper.getClassDetail(id);
    }
    /**
     * 更新班级
     *
     * @param classUpdateDTO
     * @return
     */
    @Override
    public ClassResponseDTO updateClass(ClassUpdateDTO classUpdateDTO) {
        // 参数校验
        if (classUpdateDTO == null || classUpdateDTO.getId() == null) {
            throw new IllegalArgumentException("班级更新参数不能为空");
        }
        // 更新班级
        int result = classMapper.updateClass(classUpdateDTO);
        if (result <= 0) {
            throw new RuntimeException("班级更新失败");
        }
        ClassDetailVO classDetailVO = classMapper.getClassDetail(classUpdateDTO.getId());
        if (classDetailVO == null) {
            throw new RuntimeException("班级更新失败，未查询到班级详情");
        }
        return convertToClassResponseDTO(classDetailVO);
    }
    /**
     * 删除班级
     *
     * @param ids
     * @return
     */
    @Override
    public boolean deleteClass(List<Long> ids) {
        // 参数校验
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("班级删除参数不能为空");
        }
        for (Long id : ids) {
            // 校验参数以及班级的状态
            ClassDetailVO classDetailVO = classMapper.getClassDetail(id);
            // 判断班级状态是否为在读状态，不是在读毕业的班级不能删除
            if (classDetailVO == null) {
                throw new IllegalArgumentException("班级删除参数，班级不存在");
            }
            if (!classDetailVO.getStatus().equals(CLASS_STATUS_GRADUATED)) {
                throw new IllegalArgumentException("班级删除参数，班级状态异常，只能删除毕业班级");
            }
            // 检查班级是否有关联的学生
            int relatedStudentsCount = classMapper.countRelatedStudents(id);
            if (relatedStudentsCount > 0) {
                log.warn("删除班级失败：班级下存在学生，ID={}", id);
                throw new IllegalArgumentException("该班级下存在学生，无法执行操作");
            }
        }
        // 删除班级
        int result = classMapper.deleteBatch(ids);
        if (result <= 0) {
            throw new RuntimeException("班级删除失败");
        }
        return true;
    }
    /**
     * 更新班级状态
     *
     * @param id
     * @return
     */
    @Override
    public boolean updateStatus(Long id) {
        // 校验参数以及班级的状态
        ClassDetailVO classDetailVO = classMapper.getClassDetail(id);
        if (classDetailVO == null) {
            throw new IllegalArgumentException("班级状态更新失败，班级不存在");
        }
        // 检查班级是否有关联的学生
        int relatedStudentsCount = classMapper.countRelatedStudents(id);
        if (relatedStudentsCount > 0) {
            log.warn("更新班级状态失败：班级下存在学生，ID={}", id);
            throw new IllegalArgumentException("该班级下存在学生，无法执行操作");
        }
        String currentStatus = classDetailVO.getStatus();
        String newStatus;
        if (currentStatus.equals(MessageConstant.CLASS_STATUS_ENABLED)) {
            newStatus = CLASS_STATUS_GRADUATED;
        } else {
            newStatus = MessageConstant.CLASS_STATUS_ENABLED;
        }
        // 更新班级状态
        int result = classMapper.updateStatus(id, newStatus);
        if (result <= 0) {
            throw new RuntimeException("班级状态更新失败");
        }
        return true;
    }

    private ClassResponseDTO convertToClassResponseDTO(ClassDetailVO classDetailVO) {
        ClassResponseDTO classResponseDTO = new ClassResponseDTO();
        classResponseDTO.setId(classDetailVO.getId());
        classResponseDTO.setMajorId(classDetailVO.getMajorId());
        classResponseDTO.setMajorName(classDetailVO.getMajorName());
        classResponseDTO.setClassName(classDetailVO.getClassName());
        classResponseDTO.setHeadTeacherId(classDetailVO.getHeadTeacherId());
        classResponseDTO.setHeadTeacherName(classDetailVO.getHeadTeacherName());
        classResponseDTO.setStatus(classDetailVO.getStatus());
        try {
            if (classDetailVO.getCreateTime() != null && !classDetailVO.getCreateTime().isEmpty()) {
                classResponseDTO.setCreateTime(LocalDateTime.parse(classDetailVO.getCreateTime(), DATE_TIME_FORMATTER));
            }
            if (classDetailVO.getUpdateTime() != null && !classDetailVO.getUpdateTime().isEmpty()) {
                classResponseDTO.setUpdateTime(LocalDateTime.parse(classDetailVO.getUpdateTime(), DATE_TIME_FORMATTER));
            }
        } catch (Exception e) {
            log.error("时间格式转换失败：{}", e.getMessage());
        }
        return classResponseDTO;
    }
}
