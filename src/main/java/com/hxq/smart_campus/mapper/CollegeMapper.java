package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.CollegeCreateDTO;
import com.hxq.smart_campus.entity.dto.CollegeUpdateDTO;
import com.hxq.smart_campus.entity.vo.CollegeDetailVO;
import com.hxq.smart_campus.entity.vo.CollegeListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 学院管理Mapper接口
 *
 * @author XiongQi He
 * @since 2026-04-21
 */
@Mapper
public interface CollegeMapper {

    /**
     * 获取学院列表
     *
     * @param collegeName 学院名称
     * @param status 状态
     * @return 学院列表
     */
    List<CollegeListVO> getCollegeList(@Param("collegeName") String collegeName,
                                       @Param("status") String status);

    /**
     * 新增学院
     *
     * @param collegeCreateDTO 学院创建DTO
     * @return 影响的行数
     */
    int insert(CollegeCreateDTO collegeCreateDTO);

    /**
     * 获取最后插入的ID
     *
     * @return 最后插入的ID
     */
    @Select("SELECT LAST_INSERT_ID()")
    Long getLastInsertId();

    /**
     * 获取学院详情
     *
     * @param id 学院ID
     * @return 学院详情
     */
    @Select("SELECT * FROM college WHERE id = #{id}")
    CollegeDetailVO getCollegeDetail(@Param("id") Long id);

    /**
     * 更新学院
     *
     * @param collegeUpdateDTO 学院更新DTO
     * @return 影响的行数
     */
    int update(CollegeUpdateDTO collegeUpdateDTO);

    /**
     * 批量删除学院（软删除，将状态修改为禁用）
     *
     * @param ids 学院ID列表
     * @return 影响的行数
     */
    int deleteBatch(@Param("ids") List<Long> ids);
    /**
     * 更新学院状态
     *
     * @param id 学院ID
     * @param newStatus 新状态
     * @return 影响的行数
     */
    @Update("UPDATE college SET status = #{newStatus} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("newStatus") String newStatus);

    /**
     * 查询学院关联的专业数量
     *
     * @param id 学院ID
     * @return 关联的专业数量
     */
    int countRelatedMajors(@Param("id") Long id);
    
    /**
     * 批量获取学院详情
     *
     * @param ids 学院ID列表
     * @return 学院详情列表
     */
    List<CollegeDetailVO> getCollegeDetailsByIds(@Param("ids") List<Long> ids);
    
    /**
     * 批量查询学院关联的专业数量
     *
     * @param collegeIds 学院ID列表
     * @return 关联的专业数量
     */
    int countRelatedMajorsByCollegeIds(@Param("collegeIds") List<Long> collegeIds);
}