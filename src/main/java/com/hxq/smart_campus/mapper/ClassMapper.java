package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.ClassCreateDTO;
import com.hxq.smart_campus.entity.dto.ClassUpdateDTO;
import com.hxq.smart_campus.entity.vo.ClassDetailVO;
import com.hxq.smart_campus.entity.vo.ClassListVO;
import com.hxq.smart_campus.entity.vo.StudentBasicVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ClassMapper {
    /**
     * 查询班级列表
     *
     * @param majorId
     * @param className
     * @param status
     * @return
     */
    List<ClassListVO> getClassList(@Param("majorId") Long majorId,
                                   @Param("className") String className,
                                   @Param("status") String status);
    /**
     * 新增班级
     *
     * @param classCreateDTO
     * @return
     */
    int insertClass(ClassCreateDTO classCreateDTO);
    /**
     * 获取新增班级的ID
     *
     * @return
     */
    @Select("select last_insert_id()")
    Long getLastInsertId();
    /**
     * 查询班级详情
     *
     * @param id 班级ID
     * @return 班级详情
     */
    ClassDetailVO getClassDetail(@Param("id") Long id);
    /**
     * 更新班级
     *
     * @param classUpdateDTO
     * @return
     */
    int updateClass(ClassUpdateDTO classUpdateDTO);
    /**
     * 批量删除班级
     *
     * @param ids
     * @return
     */
    int deleteBatch(@Param("ids") List<Long> ids);

    /**
     * 更新班级状态
     *
     * @param id 班级ID
     * @param newStatus 新状态
     * @return
     */
    @Update("update class set status = #{newStatus} where id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("newStatus") String newStatus);

    /**
 * 查询班级关联的学生数量
 *
 * @param id 班级ID
 * @return 关联的学生数量
 */
    int countRelatedStudents(@Param("id") Long id);

    List<StudentBasicVO> getClassStudents(@Param("classId") Long classId);

    List<ClassDetailVO> getTeachingClasses(@Param("teacherId") Long teacherId, @Param("semesterId") Long semesterId);
}
