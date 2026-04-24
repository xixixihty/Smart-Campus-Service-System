package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.ClassroomCreateDTO;
import com.hxq.smart_campus.entity.dto.ClassroomUpdateDTO;
import com.hxq.smart_campus.entity.vo.ClassroomDetailVO;
import com.hxq.smart_campus.entity.vo.ClassroomListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ClassroomMapper {

    /**
     * 根据建筑、类型和状态查询教室列表
     * @param building 建筑
     * @param type 类型
     * @param status 状态
     * @return 教室列表
     */
    List<ClassroomListVO> selectByBuildingAndTypeAndStatus(@Param("building") String building,
                                                           @Param("type") String type,
                                                           @Param("status") String status);
    /**
     * 根据ID查询教室详情
     * @param id 教室ID
     * @return 教室详情
     */
    @Select("select id, building, room_number, capacity, status, type, create_time from classroom where id = #{id}")
    ClassroomDetailVO getClassroomDetail(Long id);
    /**
     * 批量删除教室
     * @param ids 教室ID列表
     * @return 删除的行数
     */
    int deleteBatch(@Param("ids") List<Long> ids);

    /**
     * 新增教室
     * @param classroomCreateDTO
     * @return
     */
    int insertClassroom(ClassroomCreateDTO classroomCreateDTO);

    /**
     * 获取最新插入的教室ID
     * @return 最新插入的教室ID
     */
    @Select("select last_insert_id()")
    Long getLastInsertId();

    /**
     * 更新教室
     * @param classroomUpdateDTO
     * @return
     */
    int updateClassroom(ClassroomUpdateDTO classroomUpdateDTO);
}
