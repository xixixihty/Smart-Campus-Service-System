package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.StudentCreateDTO;
import com.hxq.smart_campus.entity.dto.StudentUpdateDTO;
import com.hxq.smart_campus.entity.info.MyInfo;
import com.hxq.smart_campus.entity.vo.StudentDetailVO;
import com.hxq.smart_campus.entity.vo.StudentListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface StudentMapper {
    /**
     * 新增学生
     *
     * @param studentCreateDTO
     * @return
     */
    int insert(StudentCreateDTO studentCreateDTO);

    /**
     * 获取最后插入的id
     *
     * @return
     */
    @Select("SELECT LAST_INSERT_ID()")
    Long getLastInsertId();

    /**
     * 根据id获取学生详情
     *
     * @param newStudentId
     * @return
     */
    StudentDetailVO getStudentDetail(Long newStudentId);

    /**
     * 更新学生信息
     *
     * @param studentUpdateDTO
     * @return
     */
    int update(StudentUpdateDTO studentUpdateDTO);

    /**
     * 批量删除学生
     *
     * @param ids
     * @return
     */
    int deleteBatch(@Param("ids") List<Long> ids);
    /**
     * 根据条件获取学生列表
     *
     * @param studentNo
     * @param name
     * @param classId
     * @param status
     * @param accountStatus
     * @return
     */
    List<StudentListVO> getStudentList(@Param("studentNo") String studentNo, @Param("name") String name, @Param("classId") Long classId, @Param("status") String status, @Param("accountStatus") String accountStatus);

    /**
     * 重置学生密码
     * @param id
     * @param newPassword
     * @return
     */
    @Update("update student set password=#{newPassword} where id=#{id}")
    int reSetPassword(@Param("id") Long id, @Param("newPassword") String newPassword);

    /**
     * 根据学号查询学生
     * @param studentNo 学号
     * @return 学生详情
     */
    StudentDetailVO getStudentByStudentNo(@Param("studentNo") String studentNo);
    
    /**
     * 批量根据ID获取学生详情
     * @param ids 学生ID列表
     * @return 学生详情列表
     */
    List<StudentDetailVO> getStudentDetailsByIds(@Param("ids") List<Long> ids);

    /**
     * 根据姓名查询学生详情
     * @param studentName
     * @return
     */
    StudentDetailVO getStudentDetailByName(String studentName);

    /**
     * 获取学生信息
     * @param userId
     * @return
     */
    MyInfo getStudentInfo(Long userId);
}
