package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.MajorCreateDTO;
import com.hxq.smart_campus.entity.dto.MajorUpdateDTO;
import com.hxq.smart_campus.entity.vo.MajorDetailVO;
import com.hxq.smart_campus.entity.vo.MajorListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MajorMapper {
    /**
     * 查询专业列表
     * @param collegeId
     * @param majorName
     * @param status
     * @return
     */
    List<MajorListVO> selectMajorList(@Param("collegeId") Long collegeId,
                                      @Param("majorName") String majorName,
                                      @Param("status") String status);
    /**
     * 插入专业
     * @param majorCreateDTO
     * @return
     */
    int insert(MajorCreateDTO majorCreateDTO);
    /**
     * 获取最新插入的ID
     * @return 最新插入的ID
     */
    @Select("select LAST_INSERT_ID()")
    Long getLastInsertId();

    /**
     * 获取专业详情
     * @param id 专业ID
     * @return 专业详情
     */
    MajorDetailVO getMajorDetail(@Param("id") Long id);
    /**
     * 更新专业
     * @param majorUpdateDTO
     * @return
     */
    int update(MajorUpdateDTO majorUpdateDTO);
    /**
     * 批量删除专业
     * @param ids 专业ID列表
     * @return 影响行数
     */
    int deleteBatch(@Param("ids") List<Long> ids);
    /**
     * 更新专业状态
     * @param id 专业ID
     * @param newStatus 新状态
     * @return 影响行数
     */
    @Update("update major set status = #{newStatus} where id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("newStatus") String newStatus);

    /**
     * 查询专业关联的班级数量
     * @param id 专业ID
     * @return 关联的班级数量
     */
    int countRelatedClasses(@Param("id") Long id);

    /**
     * 根据班级ID查询专业ID
     * @param classId 班级ID
     * @return
     */
    MajorDetailVO getMajorIdByClassId(Long classId);
}
