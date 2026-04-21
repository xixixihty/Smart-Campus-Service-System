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

@Mapper
public interface CollegeMapper {
    /**
     * 获取学院列表
     *
     * @param collegeName 学院名称
     * @param status      状态
     * @return 学院列表
     */
    List<CollegeListVO> getCollegeList(String collegeName, String status);
    /**
     * 新增学院
     *
     * @param collegeCreateDTO
     * @return 影响的行数
     */
    int insert(CollegeCreateDTO collegeCreateDTO);
    /**
     * 获取新增学院的ID
     *
     * @return 新增学院的ID
     */
    @Select("SELECT LAST_INSERT_ID()")
    Long getLastInsertId();
    /**
     * 获取学院详情
     *
     * @param id 学院ID
     * @return 学院详情
     */
    @Select("select * from college where id = #{id} and status = '启用'")
    CollegeDetailVO getCollegeDetail(Long id);

    /**
     * 更新学院
     *
     * @param collegeUpdateDTO 更新学院参数
     * @return 影响的行数
     */
    int update(CollegeUpdateDTO collegeUpdateDTO);
    /**
     * 批量删除学院
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
    @Update("update college set status = #{newStatus} where id = #{id}")
    int updateStatus(Long id, String newStatus);
}
