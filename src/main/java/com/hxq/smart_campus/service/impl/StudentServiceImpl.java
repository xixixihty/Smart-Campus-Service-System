package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.PasswordUpdateDTO;
import com.hxq.smart_campus.entity.dto.StudentCreateDTO;
import com.hxq.smart_campus.entity.dto.StudentResponseDTO;
import com.hxq.smart_campus.entity.dto.StudentUpdateDTO;
import com.hxq.smart_campus.entity.vo.StudentDetailVO;
import com.hxq.smart_campus.entity.vo.StudentListVO;
import com.hxq.smart_campus.mapper.StudentMapper;
import com.hxq.smart_campus.service.StudentService;
import com.hxq.smart_campus.utils.BCryptUtils;
import com.hxq.smart_campus.utils.PasswordStrengthUtils;
import com.hxq.smart_campus.utils.SensitiveInfoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.hxq.smart_campus.constant.MessageConstant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final StudentMapper studentMapper;



    /**
     * 创建学生
     * @param studentCreateDTO
     * @return
     */
    @Override
    public StudentResponseDTO createStudent(StudentCreateDTO studentCreateDTO) {
        // 校验参数
        if (studentCreateDTO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (studentCreateDTO.getStudentNo() == null || studentCreateDTO.getStudentNo().isEmpty()) {
            throw new IllegalArgumentException("学生学号不能为空");
        }
        if (studentCreateDTO.getPassword() == null || studentCreateDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        // 验证密码强度
        if (!PasswordStrengthUtils.isPasswordStrongEnough(studentCreateDTO.getPassword())) {
            throw new IllegalArgumentException(PasswordStrengthUtils.getPasswordStrengthMessage(studentCreateDTO.getPassword()));
        }
        // 记录创建学生的日志，对密码进行脱敏处理
        log.info("开始创建学生，学号：{}，姓名：{}，密码：{}", studentCreateDTO.getStudentNo(), studentCreateDTO.getName(), SensitiveInfoUtils.maskPassword(studentCreateDTO.getPassword()));
        // 对密码进行加密处理
        studentCreateDTO.setPassword(BCryptUtils.encryptPassword(studentCreateDTO.getPassword()));
        int result = studentMapper.insert(studentCreateDTO);
        if (result <= 0) {
            throw new RuntimeException("创建学生失败");
        }
        log.info("学生创建成功，学号：{}", studentCreateDTO.getStudentNo());
        // 获取创建的学生详情
        Long newStudentId = studentMapper.getLastInsertId();
        StudentDetailVO studentDetailVO = studentMapper.getStudentDetail(newStudentId);
        if (studentDetailVO == null) {
            throw new RuntimeException("创建学生失败");
        }
        // 转换为响应DTO
        return convertToStudentResponseDTO(studentDetailVO);
    }

    /**
     * 更新学生
     * @param studentUpdateDTO
     * @return
     */
    @Override
    public StudentResponseDTO updateStudent(StudentUpdateDTO studentUpdateDTO) {
        // 校验参数
        if (studentUpdateDTO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (studentUpdateDTO.getId() == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        int result = studentMapper.update(studentUpdateDTO);
        if (result <= 0) {
            throw new IllegalArgumentException("更新学生失败");
        }
        return convertToStudentResponseDTO(studentMapper.getStudentDetail(studentUpdateDTO.getId()));
    }

    /**
     * 批量删除学生
     * @param ids 学生ID列表
     * @return 是否删除成功
     */
    @Override
    public boolean deleteStudent(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空");
        }
        
        // 批量获取学生详情，减少数据库查询次数
        List<StudentDetailVO> studentDetails = studentMapper.getStudentDetailsByIds(ids);
        if (studentDetails.size() != ids.size()) {
            throw new IllegalArgumentException("部分学生不存在");
        }
        
        // 检查学生状态
        for (StudentDetailVO student : studentDetails) {
            if (!STUDENT_STATUS_DROPPED_OUT.equals(student.getStatus())) {
                throw new IllegalArgumentException("学生状态不是退学，不能删除");
            }
        }
        
        int result = studentMapper.deleteBatch(ids);
        if (result <= 0) {
            throw new IllegalArgumentException("删除学生失败");
        }
        return true;
    }

    /**
     * 查询学生列表
     * @param pageNum
     * @param pageSize
     * @param studentNo
     * @param name
     * @param classId
     * @param status
     * @param accountStatus
     * @return
     */
    @Override
    public PageInfo<StudentListVO> getStudentList(Integer pageNum, Integer pageSize, String studentNo, String name, Long classId, String status, String accountStatus) {
        PageHelper.startPage(pageNum, pageSize);
        List<StudentListVO> studentList = studentMapper.getStudentList(studentNo, name, classId, status, accountStatus);
        return PageInfo.of(studentList);
    }

    /**
     * 查询学生详情
     * @param id
     * @return
     */
    @Override
    public StudentDetailVO getStudentDetail(Long id) {
        return studentMapper.getStudentDetail(id);
    }

    /**
     * 重置学生密码
     * @param id
     * @return
     */
    @Override
    public boolean reSetPassword(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        String newPassword = BCryptUtils.encryptPassword(DEFAULT_PASSWORD);
        int result = studentMapper.reSetPassword(id, newPassword);
        if (result <= 0) {
            throw new IllegalArgumentException("重置密码失败");
        }
        return true;
    }
    /**
     * 将StudentDetailVO转换为StudentResponseDTO
     *
     * @param studentDetailVO
     * @return
     */
    private StudentResponseDTO convertToStudentResponseDTO(StudentDetailVO studentDetailVO) {
        StudentResponseDTO studentResponseDTO = new StudentResponseDTO();
        studentResponseDTO.setId(studentDetailVO.getId());
        studentResponseDTO.setStudentNo(studentDetailVO.getStudentNo());
        studentResponseDTO.setName(studentDetailVO.getName());
        studentResponseDTO.setGender(studentDetailVO.getGender());
        studentResponseDTO.setClassId(studentDetailVO.getClassId());
        studentResponseDTO.setClassName(studentDetailVO.getClassName());
        studentResponseDTO.setEnrollmentDate(studentDetailVO.getEnrollmentDate());
        studentResponseDTO.setStatus(studentDetailVO.getStatus());
        studentResponseDTO.setIdCard(studentDetailVO.getIdCard());
        studentResponseDTO.setPhone(studentDetailVO.getPhone());
        studentResponseDTO.setAccountStatus(studentDetailVO.getAccountStatus());
        try{
            if (studentDetailVO.getCreateTime() != null && !studentDetailVO.getCreateTime().isEmpty()) {
                studentResponseDTO.setCreateTime(LocalDateTime.parse(studentDetailVO.getCreateTime(), DATE_TIME_FORMATTER));
            }
            if (studentDetailVO.getUpdateTime() != null && !studentDetailVO.getUpdateTime().isEmpty()) {
                studentResponseDTO.setUpdateTime(LocalDateTime.parse(studentDetailVO.getUpdateTime(), DATE_TIME_FORMATTER));
            }
        } catch (Exception e) {
            log.error("转换为响应DTO失败", e);
        }
        return studentResponseDTO;
    }

    /**
     * 修改学生密码
     * @param passwordUpdateDTO 密码修改信息
     * @return 是否成功
     */
    @Override
    public boolean updatePassword(PasswordUpdateDTO passwordUpdateDTO) {
        // 校验参数
        if (passwordUpdateDTO == null || passwordUpdateDTO.getId() == null || passwordUpdateDTO.getOldPassword() == null || passwordUpdateDTO.getNewPassword() == null || passwordUpdateDTO.getConfirmPassword() == null) {
            throw new IllegalArgumentException("密码修改参数不能为空");
        }
        // 验证新密码和确认密码是否一致
        if (!passwordUpdateDTO.getNewPassword().equals(passwordUpdateDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("新密码和确认密码不一致");
        }
        // 获取学生信息
        StudentDetailVO student = studentMapper.getStudentDetail(passwordUpdateDTO.getId());
        if (student == null) {
            throw new IllegalArgumentException("学生不存在");
        }
        // 验证旧密码是否正确
        if (!BCryptUtils.checkPassword(passwordUpdateDTO.getOldPassword(), student.getPassword())) {
            throw new IllegalArgumentException("旧密码错误");
        }
        // 验证新密码强度
        if (!PasswordStrengthUtils.isPasswordStrongEnough(passwordUpdateDTO.getNewPassword())) {
            throw new IllegalArgumentException(PasswordStrengthUtils.getPasswordStrengthMessage(passwordUpdateDTO.getNewPassword()));
        }
        // 记录修改密码的日志，对密码进行脱敏处理
        log.info("开始修改学生密码，ID：{}，旧密码：{}，新密码：{}", passwordUpdateDTO.getId(), SensitiveInfoUtils.maskPassword(passwordUpdateDTO.getOldPassword()), SensitiveInfoUtils.maskPassword(passwordUpdateDTO.getNewPassword()));
        // 对新密码进行加密处理
        String encryptedPassword = BCryptUtils.encryptPassword(passwordUpdateDTO.getNewPassword());
        // 更新密码
        int result = studentMapper.reSetPassword(passwordUpdateDTO.getId(), encryptedPassword);
        if (result <= 0) {
            throw new IllegalArgumentException("修改密码失败");
        }
        log.info("学生密码修改成功，ID：{}", passwordUpdateDTO.getId());
        return true;
    }

    /**
     * 锁定学生账号
     * @param id 学生ID
     * @return 是否成功
     */
    @Override
    public boolean lockAccount(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        // 获取学生信息
        StudentDetailVO student = studentMapper.getStudentDetail(id);
        if (student == null) {
            throw new IllegalArgumentException("学生不存在");
        }
        // 检查账号状态是否已经是锁定
        if ("锁定".equals(student.getAccountStatus())) {
            throw new IllegalArgumentException("账号已经是锁定状态");
        }
        // 创建更新DTO
        StudentUpdateDTO updateDTO = new StudentUpdateDTO();
        updateDTO.setId(id);
        updateDTO.setAccountStatus("锁定");
        // 更新账号状态
        int result = studentMapper.update(updateDTO);
        if (result <= 0) {
            throw new IllegalArgumentException("锁定账号失败");
        }
        log.info("学生账号锁定成功，ID：{}", id);
        return true;
    }

    /**
     * 解锁学生账号
     * @param id 学生ID
     * @return 是否成功
     */
    @Override
    public boolean unlockAccount(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        // 获取学生信息
        StudentDetailVO student = studentMapper.getStudentDetail(id);
        if (student == null) {
            throw new IllegalArgumentException("学生不存在");
        }
        // 检查账号状态是否已经是正常
        if ("正常".equals(student.getAccountStatus())) {
            throw new IllegalArgumentException("账号已经是正常状态");
        }
        // 创建更新DTO
        StudentUpdateDTO updateDTO = new StudentUpdateDTO();
        updateDTO.setId(id);
        updateDTO.setAccountStatus("正常");
        // 更新账号状态
        int result = studentMapper.update(updateDTO);
        if (result <= 0) {
            throw new IllegalArgumentException("解锁账号失败");
        }
        log.info("学生账号解锁成功，ID：{}", id);
        return true;
    }
}
