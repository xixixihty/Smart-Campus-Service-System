package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.PasswordUpdateDTO;
import com.hxq.smart_campus.entity.dto.TeacherCreateDTO;
import com.hxq.smart_campus.entity.dto.TeacherResponseDTO;
import com.hxq.smart_campus.entity.dto.TeacherUpdateDTO;
import com.hxq.smart_campus.entity.vo.TeacherDetailVO;
import com.hxq.smart_campus.entity.vo.TeacherListVO;
import com.hxq.smart_campus.mapper.TeacherMapper;
import com.hxq.smart_campus.service.TeacherService;
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
public class TeacherServiceImpl implements TeacherService {
    private final TeacherMapper teacherMapper;


    /**
     * 创建教师
     * @param teacherCreateDTO
     * @return
     */
    @Override
    public TeacherResponseDTO createTeacher(TeacherCreateDTO teacherCreateDTO) {
        // 校验参数是否为空
        if (teacherCreateDTO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // 校验工号是否已存在
        if (teacherCreateDTO.getTeacherNo() == null || teacherCreateDTO.getTeacherNo().isEmpty()) {
            throw new IllegalArgumentException("工号不能为空");
        }
        if (teacherCreateDTO.getPassword() == null || teacherCreateDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        // 验证密码强度
        if (!PasswordStrengthUtils.isPasswordStrongEnough(teacherCreateDTO.getPassword())) {
            throw new IllegalArgumentException(PasswordStrengthUtils.getPasswordStrengthMessage(teacherCreateDTO.getPassword()));
        }
        // 记录创建教师的日志，对密码进行脱敏处理
        log.info("开始创建教师，工号：{}，姓名：{}，密码：{}", teacherCreateDTO.getTeacherNo(), teacherCreateDTO.getName(), SensitiveInfoUtils.maskPassword(teacherCreateDTO.getPassword()));
        // 对密码进行加密处理
        teacherCreateDTO.setPassword(BCryptUtils.encryptPassword(teacherCreateDTO.getPassword()));
        int result = teacherMapper.insert(teacherCreateDTO);
        if (result == 0) {
            throw new IllegalArgumentException("创建教师失败");
        }
        log.info("教师创建成功，工号：{}", teacherCreateDTO.getTeacherNo());
        // 获取最新插入的教师ID
        Long id = teacherMapper.getLastInsertId();
        if (id == null) {
            throw new IllegalArgumentException("获取最新插入的教师ID失败");
        }
        // 校据ID查询教师详情
        TeacherDetailVO teacherDetailVO = teacherMapper.getTeacherDetail(id);
        if (teacherDetailVO == null) {
            throw new IllegalArgumentException("查询教师详情失败");
        }
        // 转换为响应DTO
        return convertToResponseDTO(teacherDetailVO);
    }
    /**
     * 更新教师
     */
    @Override
    public TeacherResponseDTO updateTeacher(TeacherUpdateDTO teacherUpdateDTO) {
        // 校验参数是否为空
        if (teacherUpdateDTO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        int result = teacherMapper.update(teacherUpdateDTO);
        if (result == 0) {
            throw new IllegalArgumentException("更新教师失败");
        }
        // 校据ID查询教师详情
        TeacherDetailVO teacherDetailVO = teacherMapper.getTeacherDetail(teacherUpdateDTO.getId());
        if (teacherDetailVO == null) {
            throw new IllegalArgumentException("查询教师详情失败");
        }
        // 转换为响应DTO
        return convertToResponseDTO(teacherDetailVO);
    }
    /**
     * 批量删除教师
     * @param ids 教师ID列表
     * @return 是否删除成功
     */
    @Override
    public boolean deleteByIds(List<Long> ids) {
        // 校验参数是否为空
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("ID列表不能为空");
        }
        
        // 批量获取教师详情，减少数据库查询次数
        List<TeacherDetailVO> teacherDetails = teacherMapper.getTeacherDetailsByIds(ids);
        if (teacherDetails.size() != ids.size()) {
            throw new IllegalArgumentException("部分教师不存在");
        }
        
        // 校验教师的状态是否为离职状态，不是离职状态的不能删除
        for (TeacherDetailVO teacher : teacherDetails) {
            if (TEACHER_STATUS_DISABLED.equals(teacher.getStatus())) {
                throw new IllegalArgumentException("教师状态不是离职状态，不能删除！");
            }
        }
        
        // 删除教师
        int result = teacherMapper.deleteBatch(ids);
        if (result == 0) {
            throw new IllegalArgumentException("删除教师失败");
        }
        return true;
    }
    /**
     * 分页查询教师列表
     */
    @Override
    public PageInfo<TeacherListVO> getTeacherList(Integer pageNum,
                                                  Integer pageSize,
                                                  String teacherNo,
                                                  String name,
                                                  String gender,
                                                  Long collegeId,
                                                  String status,
                                                  String accountStatus) {
        PageHelper.startPage(pageNum, pageSize);
        List<TeacherListVO> teacherListVOList = teacherMapper.getTeacherList(
                teacherNo,
                name,
                gender,
                collegeId,
                status,
                accountStatus);
        return new PageInfo<>(teacherListVOList);
    }
    /**
     * 根据ID查询教师详情
     */
    @Override
    public TeacherDetailVO getTeacherDetail(Long id) {
        return teacherMapper.getTeacherDetail(id);
    }
    /**
     * 重置教师密码
     */
    @Override
    public boolean reSetPassword(Long id) {
        // 校验参数是否存在
        TeacherDetailVO teacherDetailVO = teacherMapper.getTeacherDetail(id);
        if (teacherDetailVO == null) {
            throw new IllegalArgumentException("教师不存在，ID=" + id);
        }

        // 重置密码
        String encryptedPassword = BCryptUtils.encryptPassword(DEFAULT_PASSWORD);
        // 更新教师密码
        int result = teacherMapper.reSetPassword(id, encryptedPassword);
        if (result <= 0) {
            throw new IllegalArgumentException("重置密码失败");
        }
        return true;
    }

    /**
     * 转换为响应DTO
     * @param teacherDetailVO
     * @return
     */
    private TeacherResponseDTO convertToResponseDTO(TeacherDetailVO teacherDetailVO) {
        if (teacherDetailVO == null) {
            return null;
        }
        TeacherResponseDTO teacherResponseDTO = new TeacherResponseDTO();
        teacherResponseDTO.setId(teacherDetailVO.getTeacherId());
        teacherResponseDTO.setTeacherNo(teacherDetailVO.getTeacherNo());
        teacherResponseDTO.setName(teacherDetailVO.getName());
        teacherResponseDTO.setGender(teacherDetailVO.getGender());
        teacherResponseDTO.setCollegeId(teacherDetailVO.getCollegeId());
        teacherResponseDTO.setCollegeName(teacherDetailVO.getCollegeName());
        teacherResponseDTO.setTitle(teacherDetailVO.getTitle());
        teacherResponseDTO.setPhone(teacherDetailVO.getPhone());
        teacherResponseDTO.setEmail(teacherDetailVO.getEmail());
        teacherResponseDTO.setStatus(teacherDetailVO.getStatus());
        teacherResponseDTO.setAccountStatus(teacherDetailVO.getAccountStatus());
        try {
            if (teacherDetailVO.getCreateTime() != null && !teacherDetailVO.getCreateTime().isEmpty()) {
                teacherResponseDTO.setCreateTime(LocalDateTime.parse(teacherDetailVO.getCreateTime(), DATE_TIME_FORMATTER));
            }
            if (teacherDetailVO.getUpdateTime() != null && !teacherDetailVO.getUpdateTime().isEmpty()) {
                teacherResponseDTO.setUpdateTime(LocalDateTime.parse(teacherDetailVO.getUpdateTime(), DATE_TIME_FORMATTER));
            }
        } catch (Exception e) {
            log.error("转换为响应DTO失败", e);
        }
        return teacherResponseDTO;
    }

    /**
     * 修改教师密码
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
        // 获取教师信息
        TeacherDetailVO teacher = teacherMapper.getTeacherDetail(passwordUpdateDTO.getId());
        if (teacher == null) {
            throw new IllegalArgumentException("教师不存在");
        }
        // 验证旧密码是否正确
        if (!BCryptUtils.checkPassword(passwordUpdateDTO.getOldPassword(), teacher.getPassword())) {
            throw new IllegalArgumentException("旧密码错误");
        }
        // 验证新密码强度
        if (!PasswordStrengthUtils.isPasswordStrongEnough(passwordUpdateDTO.getNewPassword())) {
            throw new IllegalArgumentException(PasswordStrengthUtils.getPasswordStrengthMessage(passwordUpdateDTO.getNewPassword()));
        }
        // 记录修改密码的日志，对密码进行脱敏处理
        log.info("开始修改教师密码，ID：{}，旧密码：{}，新密码：{}", passwordUpdateDTO.getId(), SensitiveInfoUtils.maskPassword(passwordUpdateDTO.getOldPassword()), SensitiveInfoUtils.maskPassword(passwordUpdateDTO.getNewPassword()));
        // 对新密码进行加密处理
        String encryptedPassword = BCryptUtils.encryptPassword(passwordUpdateDTO.getNewPassword());
        // 更新密码
        int result = teacherMapper.reSetPassword(passwordUpdateDTO.getId(), encryptedPassword);
        if (result <= 0) {
            throw new IllegalArgumentException("修改密码失败");
        }
        log.info("教师密码修改成功，ID：{}", passwordUpdateDTO.getId());
        return true;
    }

    /**
     * 锁定教师账号
     * @param id 教师ID
     * @return 是否成功
     */
    @Override
    public boolean lockAccount(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("教师ID不能为空");
        }
        // 获取教师信息
        TeacherDetailVO teacher = teacherMapper.getTeacherDetail(id);
        if (teacher == null) {
            throw new IllegalArgumentException("教师不存在");
        }
        // 检查账号状态是否已经是锁定
        if ("锁定".equals(teacher.getAccountStatus())) {
            throw new IllegalArgumentException("账号已经是锁定状态");
        }
        // 创建更新DTO
        TeacherUpdateDTO updateDTO = new TeacherUpdateDTO();
        updateDTO.setId(id);
        updateDTO.setAccountStatus("锁定");
        // 更新账号状态
        int result = teacherMapper.update(updateDTO);
        if (result <= 0) {
            throw new IllegalArgumentException("锁定账号失败");
        }
        log.info("教师账号锁定成功，ID：{}", id);
        return true;
    }

    /**
     * 解锁教师账号
     * @param id 教师ID
     * @return 是否成功
     */
    @Override
    public boolean unlockAccount(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("教师ID不能为空");
        }
        // 获取教师信息
        TeacherDetailVO teacher = teacherMapper.getTeacherDetail(id);
        if (teacher == null) {
            throw new IllegalArgumentException("教师不存在");
        }
        // 检查账号状态是否已经是正常
        if ("正常".equals(teacher.getAccountStatus())) {
            throw new IllegalArgumentException("账号已经是正常状态");
        }
        // 创建更新DTO
        TeacherUpdateDTO updateDTO = new TeacherUpdateDTO();
        updateDTO.setId(id);
        updateDTO.setAccountStatus("正常");
        // 更新账号状态
        int result = teacherMapper.update(updateDTO);
        if (result <= 0) {
            throw new IllegalArgumentException("解锁账号失败");
        }
        log.info("教师账号解锁成功，ID：{}", id);
        return true;
    }
}
