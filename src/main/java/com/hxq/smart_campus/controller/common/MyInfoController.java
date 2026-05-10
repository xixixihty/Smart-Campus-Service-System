package com.hxq.smart_campus.controller.common;

import com.hxq.smart_campus.entity.dto.PasswordUpdateDTO;
import com.hxq.smart_campus.entity.info.MyInfo;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.MyInfoService;
import com.hxq.smart_campus.service.StudentService;
import com.hxq.smart_campus.service.TeacherService;
import com.hxq.smart_campus.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.hxq.smart_campus.constant.MessageConstant.USER_TYPE_STUDENT;
import static com.hxq.smart_campus.constant.MessageConstant.USER_TYPE_TEACHER;

@RestController
@RequestMapping("/api/my-infos")
@Slf4j
@Tag(name = "个人信息接口")
@RequiredArgsConstructor
public class MyInfoController {
    private final MyInfoService myInfoService;
    private final TeacherService teacherService;
    private final StudentService studentService;

    /**
     * 获取当前用户信息
     * @return
     */
    @GetMapping
    @Operation(summary = "获取当前用户信息")
    public Result<MyInfo> getMyInfo() {
        log.info("获取当前用户信息");
        MyInfo myInfo = myInfoService.getMyInfo();
        return Result.success(myInfo);
    }

    /**
     * 修改当前用户密码
     * @param passwordUpdateDTO 密码修改信息
     * @return
     */
    @PutMapping("/password")
    @Operation(summary = "修改当前用户密码")
    public Result<Boolean> updatePassword(@RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        log.info("修改当前用户密码");
        Long userId = SecurityUtils.getCurrentUserId();
        String userType = SecurityUtils.getCurrentUserType();
        if (userId == null || userType == null) {
            return Result.error("用户未登录");
        }
        passwordUpdateDTO.setId(userId);
        boolean result;
        if (USER_TYPE_TEACHER.equals(userType)) {
            result = teacherService.updatePassword(passwordUpdateDTO);
        } else if (USER_TYPE_STUDENT.equals(userType)) {
            result = studentService.updatePassword(passwordUpdateDTO);
        } else {
            return Result.error("用户类型错误");
        }
        return Result.success(result);
    }
}
