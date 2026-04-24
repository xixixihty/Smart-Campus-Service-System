package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.PasswordUpdateDTO;
import com.hxq.smart_campus.entity.dto.TeacherCreateDTO;
import com.hxq.smart_campus.entity.dto.TeacherResponseDTO;
import com.hxq.smart_campus.entity.dto.TeacherUpdateDTO;
import com.hxq.smart_campus.entity.vo.TeacherDetailVO;
import com.hxq.smart_campus.entity.vo.TeacherListVO;

import java.util.List;

public interface TeacherService {
    /**
     * 创建教师
     */
    TeacherResponseDTO createTeacher(TeacherCreateDTO teacherCreateDTO);
    /**
     * 更新教师
     */
    TeacherResponseDTO updateTeacher(TeacherUpdateDTO teacherUpdateDTO);
    /**
     * 批量删除教师
     */
    boolean deleteByIds(List<Long> ids);
    /**
     * 分页查询教师列表
     */
    PageInfo<TeacherListVO> getTeacherList(Integer pageNum,
                                           Integer pageSize,
                                           String teacherNo,
                                           String name,
                                           String gender,
                                           Long collegeId,
                                           String status,
                                           String accountStatus);
    /**
     * 根据ID查询教师详情
     */
    TeacherDetailVO getTeacherDetail(Long id);
    /**
     * 重置教师密码
     */
    boolean reSetPassword(Long id);

    /**
     * 修改教师密码
     * @param passwordUpdateDTO 密码修改信息
     * @return 是否成功
     */
    boolean updatePassword(PasswordUpdateDTO passwordUpdateDTO);

    /**
     * 锁定教师账号
     * @param id 教师ID
     * @return 是否成功
     */
    boolean lockAccount(Long id);

    /**
     * 解锁教师账号
     * @param id 教师ID
     * @return 是否成功
     */
    boolean unlockAccount(Long id);
}
