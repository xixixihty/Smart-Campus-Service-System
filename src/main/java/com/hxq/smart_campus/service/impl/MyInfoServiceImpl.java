package com.hxq.smart_campus.service.impl;

import com.hxq.smart_campus.entity.info.MyInfo;
import com.hxq.smart_campus.mapper.StudentMapper;
import com.hxq.smart_campus.mapper.TeacherMapper;
import com.hxq.smart_campus.service.MyInfoService;
import com.hxq.smart_campus.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.hxq.smart_campus.constant.MessageConstant.USER_TYPE_STUDENT;
import static com.hxq.smart_campus.constant.MessageConstant.USER_TYPE_TEACHER;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyInfoServiceImpl implements MyInfoService {
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    /**
     * 获取当前用户信息
     * @return
     */
    @Override
    public MyInfo getMyInfo() {
        // 根据登陆信息获取当前用户的ID以及用户类型
        Long userId = SecurityUtils.getCurrentUserId();
        String userType = SecurityUtils.getCurrentUserType();
        // 判断用户ID以及用户类型
        if (userId == null || userType == null) {
            throw new RuntimeException("用户未登录");
        }
        // 判断用户类型
        if (USER_TYPE_TEACHER.equals(userType)) {
            // 用户为教师
            MyInfo myInfo = teacherMapper.getTeacherInfo(userId);
            if (myInfo == null) {
                throw new RuntimeException("教师不存在");
            }
            return myInfo;
        } else if (USER_TYPE_STUDENT.equals(userType)) {
            // 用户为学生
            MyInfo myInfo = studentMapper.getStudentInfo(userId);
            if (myInfo == null) {
                throw new RuntimeException("学生不存在");
            }
            return myInfo;
        } else {
            throw new RuntimeException("用户类型错误");
        }
    }
}
