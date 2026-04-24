package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.PasswordUpdateDTO;
import com.hxq.smart_campus.entity.dto.StudentCreateDTO;
import com.hxq.smart_campus.entity.dto.StudentResponseDTO;
import com.hxq.smart_campus.entity.dto.StudentUpdateDTO;
import com.hxq.smart_campus.entity.vo.StudentDetailVO;
import com.hxq.smart_campus.entity.vo.StudentListVO;

import java.util.List;

public interface StudentService {
    /**
     * 创建学生
     *
     * @param studentCreateDTO
     * @return
     */
    StudentResponseDTO createStudent(StudentCreateDTO studentCreateDTO);

    /**
     * 更新学生
     *
     * @param studentUpdateDTO
     * @return
     */
    StudentResponseDTO updateStudent(StudentUpdateDTO studentUpdateDTO);

    /**
     * 删除学生
     *
     * @param ids
     * @return
     */
    boolean deleteStudent(List<Long> ids);

    /**
     * 获取学生列表
     *
     * @param pageNum
     * @param pageSize
     * @param studentNo
     * @param name
     * @param classId
     * @param status
     * @param accountStatus
     * @return
     */
    PageInfo<StudentListVO> getStudentList(Integer pageNum,
                                           Integer pageSize,
                                           String studentNo,
                                           String name,
                                           Long classId,
                                           String status,
                                           String accountStatus);

    /**
     * 获取学生详情
     *
     * @param id
     * @return
     */
    StudentDetailVO getStudentDetail(Long id);

    /**
     * 重置密码
     * @param id
     * @return
     */
    boolean reSetPassword(Long id);

    /**
     * 修改密码
     * @param passwordUpdateDTO 密码修改信息
     * @return 是否成功
     */
    boolean updatePassword(PasswordUpdateDTO passwordUpdateDTO);

    /**
     * 锁定学生账号
     * @param id 学生ID
     * @return 是否成功
     */
    boolean lockAccount(Long id);

    /**
     * 解锁学生账号
     * @param id 学生ID
     * @return 是否成功
     */
    boolean unlockAccount(Long id);
}
