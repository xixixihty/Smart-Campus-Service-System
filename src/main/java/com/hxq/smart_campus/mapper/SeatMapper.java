package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.SeatCreateDTO;
import com.hxq.smart_campus.entity.dto.SeatUpdateDTO;
import com.hxq.smart_campus.entity.vo.SeatDetailVO;
import com.hxq.smart_campus.entity.vo.SeatListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SeatMapper {
    /**
     * 新增座位
     * @param seatCreateDTO 座位创建DTO
     * @return 影响行数
     */
    int insertSeat(SeatCreateDTO seatCreateDTO);

    /**
     * 获取新增座位的ID
     * @return 最新插入ID
     */
    @Select("select last_insert_id()")
    Long getLastInsertId();

    /**
     * 获取座位详情
     * @param id 座位ID
     * @return 座位详情VO
     */
    SeatDetailVO getSeatDetail(Long id);

    /**
     * 更新座位
     * @param seatUpdateDTO 座位更新DTO
     * @return 影响行数
     */
    int updateSeat(SeatUpdateDTO seatUpdateDTO);

    /**
     * 批量删除座位
     * @param ids 座位ID列表
     * @return 影响行数
     */
    int deleteBatch(@Param("ids") List<Long> ids);

    /**
     * 获取座位列表
     * @param roomId 阅览室ID
     * @param status 状态
     * @return 座位列表
     */
    List<SeatListVO> getSeatList(@Param("roomId") Long roomId, @Param("status") String status);
}
