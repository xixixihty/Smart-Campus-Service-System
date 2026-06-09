package com.hxq.smart_campus.service.impl;

import com.hxq.smart_campus.entity.dto.LoginDTO;
import com.hxq.smart_campus.entity.dto.LoginResponseDTO;
import com.hxq.smart_campus.entity.vo.AdminDetailVO;
import com.hxq.smart_campus.entity.vo.StudentDetailVO;
import com.hxq.smart_campus.entity.vo.TeacherDetailVO;
import com.hxq.smart_campus.mapper.AdminMapper;
import com.hxq.smart_campus.mapper.StudentMapper;
import com.hxq.smart_campus.mapper.TeacherMapper;
import com.hxq.smart_campus.service.AuthService;
import com.hxq.smart_campus.util.BCryptUtils;
import com.hxq.smart_campus.util.JwtUtils;
import com.hxq.smart_campus.util.SensitiveInfoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static com.hxq.smart_campus.constant.MessageConstant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final AdminMapper adminMapper;

    @Override
    public LoginResponseDTO login(LoginDTO loginDTO) {
        if (loginDTO == null || loginDTO.getUsername() == null || loginDTO.getPassword() == null || loginDTO.getUserType() == null) {
            throw new IllegalArgumentException("登录参数不能为空");
        }

        log.info("用户登录尝试，用户名：{}，用户类型：{}，密码：{}", loginDTO.getUsername(), loginDTO.getUserType(), SensitiveInfoUtils.maskPassword(loginDTO.getPassword()));

        LoginResponseDTO responseDTO = new LoginResponseDTO();
        responseDTO.setUserType(loginDTO.getUserType());

        log.debug("准备验证密码，用户输入长度：{}", loginDTO.getPassword().length());
        log.debug("当前密码验证逻辑：直接使用明文密码与数据库哈希比对");

        if (USER_TYPE_STUDENT.equals(loginDTO.getUserType())) {
            StudentDetailVO student = studentMapper.getStudentByStudentNo(loginDTO.getUsername());
            if (student == null) {
                throw new IllegalArgumentException("学生不存在");
            }
            if (STUDENT_ACCOUNT_STATUS_LOCK.equals(student.getAccountStatus())) {
                throw new IllegalArgumentException("账号已被锁定");
            }
            if (!BCryptUtils.checkPassword(loginDTO.getPassword(), student.getPassword())) {
                throw new IllegalArgumentException("密码错误");
            }
            responseDTO.setUserId(student.getId());
            responseDTO.setUsername(student.getStudentNo());
            responseDTO.setName(student.getName());
            responseDTO.setStatus(student.getStatus());
            responseDTO.setAccountStatus(student.getAccountStatus());
        } else if (USER_TYPE_TEACHER.equals(loginDTO.getUserType())) {
            TeacherDetailVO teacher = teacherMapper.getTeacherByTeacherNo(loginDTO.getUsername());
            if (teacher == null) {
                throw new IllegalArgumentException("教师不存在");
            }
            if (TEACHER_ACCOUNT_STATUS_LOCK.equals(teacher.getAccountStatus())) {
                throw new IllegalArgumentException("账号已被锁定");
            }
            if (!BCryptUtils.checkPassword(loginDTO.getPassword(), teacher.getPassword())) {
                throw new IllegalArgumentException("密码错误");
            }
            responseDTO.setUserId(teacher.getTeacherId());
            responseDTO.setUsername(teacher.getTeacherNo());
            responseDTO.setName(teacher.getName());
            responseDTO.setStatus(teacher.getStatus());
            responseDTO.setAccountStatus(teacher.getAccountStatus());
        } else if (USER_TYPE_ADMIN.equals(loginDTO.getUserType())) {
            AdminDetailVO admin = adminMapper.getAdminByAdminNo(loginDTO.getUsername());
            if (admin == null) {
                throw new IllegalArgumentException("管理员不存在");
            }
            if (ADMIN_ACCOUNT_STATUS_LOCK.equals(admin.getAccountStatus())) {
                throw new IllegalArgumentException("账号已被锁定");
            }
            if (!BCryptUtils.checkPassword(loginDTO.getPassword(), admin.getPassword())) {
                throw new IllegalArgumentException("密码错误");
            }
            responseDTO.setUserId(admin.getId());
            responseDTO.setUsername(admin.getAdminNo());
            responseDTO.setName(admin.getName());
            responseDTO.setStatus("启用");
            responseDTO.setAccountStatus(admin.getAccountStatus());
        } else {
            throw new IllegalArgumentException("用户类型错误");
        }

        String token = JwtUtils.generateToken(
                responseDTO.getUserId(),
                responseDTO.getUsername(),
                responseDTO.getName(),
                responseDTO.getUserType()
        );
        responseDTO.setToken(token);
        responseDTO.setExpireTime(LocalDateTime.now().plus(24, ChronoUnit.HOURS));

        log.info("用户登录成功：{}，类型：{}", loginDTO.getUsername(), loginDTO.getUserType());
        return responseDTO;
    }
}