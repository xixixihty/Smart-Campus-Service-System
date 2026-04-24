package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.TeacherCreateDTO;
import com.hxq.smart_campus.entity.dto.TeacherUpdateDTO;
import com.hxq.smart_campus.entity.vo.TeacherDetailVO;
import com.hxq.smart_campus.entity.vo.TeacherListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


import java.util.List;

@Mapper
public interface TeacherMapper {
    /**
     * 创建教师
     * @param teacherCreateDTO
     * @return
     */
    int insert(TeacherCreateDTO teacherCreateDTO);

    /**
     * 获取最新插入的教师ID
     * @return
     */
    @Select("select LAST_INSERT_ID()")
    Long getLastInsertId();
    /**
     * 根据ID查询教师详情
     * @param id
     * @return
     */
    TeacherDetailVO getTeacherDetail(Long id);
    /**
     * 更新教师
     * @param teacherUpdateDTO
     * @return
     */
    int update(TeacherUpdateDTO teacherUpdateDTO);
    /**
     * 根据ID删除教师
     * @param ids
     * @return
     */
    int deleteBatch(@Param("ids") List<Long> ids);
    /**
     * 根据条件查询教师列表
     * @param teacherNo
     * @param name
     * @param gender
     * @param collegeId
     * @param status
     * @param accountStatus
     * @return
     */
    List<TeacherListVO> getTeacherList(@Param("teacherNo") String teacherNo,
                                       @Param("name") String name,
                                       @Param("gender") String gender,
                                       @Param("collegeId") Long collegeId,
                                       @Param("status") String status,
                                       @Param("accountStatus") String accountStatus);
    /**
     * 重置教师密码
     * @param id
     * @param encryptedPassword
     * @return
     */
    @Update("update teacher set password = #{encryptedPassword} where id = #{id}")
    int reSetPassword(@Param("id") Long id, @Param("encryptedPassword") String encryptedPassword);

    /**
     * 根据工号查询教师
     * @param teacherNo 工号
     * @return 教师详情
     */
    TeacherDetailVO getTeacherByTeacherNo(@Param("teacherNo") String teacherNo);
    
    /**
     * 批量根据ID获取教师详情
     * @param ids 教师ID列表
     * @return 教师详情列表
     */
    List<TeacherDetailVO> getTeacherDetailsByIds(@Param("ids") List<Long> ids);
}
