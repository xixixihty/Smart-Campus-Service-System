package com.hxq.smart_campus.service.impl;

import com.hxq.smart_campus.entity.dto.LoginDTO;
import com.hxq.smart_campus.entity.dto.LoginResponseDTO;
import com.hxq.smart_campus.entity.vo.StudentDetailVO;
import com.hxq.smart_campus.entity.vo.TeacherDetailVO;
import com.hxq.smart_campus.mapper.StudentMapper;
import com.hxq.smart_campus.mapper.TeacherMapper;
import com.hxq.smart_campus.service.AuthService;
import com.hxq.smart_campus.utils.BCryptUtils;
import com.hxq.smart_campus.utils.JwtUtils;
import com.hxq.smart_campus.utils.SensitiveInfoUtils;
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

    @Override
    public LoginResponseDTO login(LoginDTO loginDTO) {
        // 校验参数
        if (loginDTO == null || loginDTO.getUsername() == null || loginDTO.getPassword() == null || loginDTO.getUserType() == null) {
            throw new IllegalArgumentException("登录参数不能为空");
        }

        // 记录登录尝试的日志，对密码进行脱敏处理
        log.info("用户登录尝试，用户名：{}，用户类型：{}，密码：{}", loginDTO.getUsername(), loginDTO.getUserType(), SensitiveInfoUtils.maskPassword(loginDTO.getPassword()));

        LoginResponseDTO responseDTO = new LoginResponseDTO();
        responseDTO.setUserType(loginDTO.getUserType());


        // 密码验证前的日志记录
        log.debug("准备验证密码，用户输入长度：{}", loginDTO.getPassword().length());
        log.debug("当前密码验证逻辑：直接使用明文密码与数据库哈希比对");

        if (USER_TYPE_STUDENT.equals(loginDTO.getUserType())) {
            // 学生登录
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
            // 教师登录
            TeacherDetailVO teacher = teacherMapper.getTeacherByTeacherNo(loginDTO.getUsername());
            if (teacher == null) {
                throw new IllegalArgumentException("教师不存在");
            }
            if ("锁定".equals(teacher.getAccountStatus())) {
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
        } else {
            throw new IllegalArgumentException("用户类型错误");
        }

        // 生成JWT令牌
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
