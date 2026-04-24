package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.SemesterCreateDTO;
import com.hxq.smart_campus.entity.dto.SemesterUpdateDTO;
import com.hxq.smart_campus.entity.vo.SemesterDetailVO;
import com.hxq.smart_campus.entity.vo.SemesterListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SemesterMapper {


    /**
     * 获取学期列表
     * @param name
     * @param status
     * @return
     */
    List<SemesterListVO> getSemesterList(@Param("name") String name,
                                         @Param("status") String status);
    /**
     * 获取学期详情
     * @param id
     * @return
     */
    @Select("select id, name, start_date, end_date, is_current, status, create_time, update_time from semester where id = #{id}")
    SemesterDetailVO getSemesterDetail(Long id);

    /**
     * 创建学期
     * @param semesterCreateDTO
     * @return
     */
    int insertSemester(SemesterCreateDTO semesterCreateDTO);
    /**
     * 获取最新插入的学期ID
     * @return
     */
    @Select("select last_insert_id()")
    Long getLastInsertId();
    /**
     * 更新学期
     * @param semesterUpdateDTO
     * @return
     */
    int updateSemester(SemesterUpdateDTO semesterUpdateDTO);
    /**
     * 批量删除学期
     * @param ids
     * @return
     */
    int deleteBatch(@Param("ids") List<Long> ids);
    /**
     * 获取当前学期
     * @return
     */
    @Select("select id, name, start_date, end_date, is_current, status, create_time, update_time from semester where is_current = 1")
    SemesterDetailVO getCurrentSemester();

    /**
     * 设置当前学期
     * @param id 学期ID
     * @param b 是否为当前学期
     * @return 是否更新成功
     */
    int updateIsCurrentById(@Param("id") Long id, @Param("isCurrent") Boolean b);
    
    /**
     * 将所有学期设置为非当前学期
     * @return 更新的记录数
     */
    int updateAllSemestersToNonCurrent();
}
