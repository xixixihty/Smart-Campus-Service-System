package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.SeatReservationCreateDTO;
import com.hxq.smart_campus.entity.vo.SeatReservationDetailVO;
import com.hxq.smart_campus.entity.vo.SeatReservationListVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SeatReservationMapper {
    /**
     * 座位预约
     * @param seatReservationCreateDTO
     * @return
     */
    int insertSeatReservation(SeatReservationCreateDTO seatReservationCreateDTO);

    /**
     * 获取最新插入的ID
     * @return
     */
    @Select("select last_insert_id()")
    Long getLastInsertId();

    /**
     * 获取座位预约详情
     * @param id
     * @return
     */
    SeatReservationDetailVO getSeatReservationDetail(Long id);

    /**
     * 取消预约（仅当状态为待签到或已过期时才能取消）
     * @param id 预约ID
     * @return 影响行数
     */
    @Delete("delete from seat_reservation where id = #{id} and status in ('待签到', '已过期', '已取消')")
    int deleteSeatReservation(Long id);

    /**
     * 签到座位预约（仅当状态为待签到时才能签到）
     * @param id 预约ID
     * @return 影响行数
     */
    @Update("update seat_reservation set status = '使用中' where id = #{id} and status = '待签到'")
    int checkInSeatReservation(Long id);

    /**
     * 签退座位预约（仅当状态为使用中或暂离时才能签退）
     * @param id 预约ID
     * @return 影响行数
     */
    @Update("update seat_reservation set status = '已完成', leave_time = NOW() where id = #{id} and status in ('使用中', '暂离')")
    int checkOutSeatReservation(Long id);

    /**
     * 暂离座位预约（仅当状态为使用中时才能暂离）
     * @param id 预约ID
     * @return 影响行数
     */
    @Update("update seat_reservation set status = '暂离', leave_time = NOW() where id = #{id} and status = '使用中'")
    int leaveSeatReservation(Long id);

    /**
     * 获取座位预约列表
     * @param userId 用户ID
     * @param seatId 座位ID
     * @param date 预约日期
     * @param status 状态
     * @return 预约列表
     */
    List<SeatReservationListVO> getSeatReservationList(@Param("userId") Long userId,
                                                       @Param("seatId") Long seatId,
                                                       @Param("date") LocalDate date,
                                                       @Param("status") String status);

    /**
     * 检查座位时间冲突
     * @param seatId 座位ID
     * @param date 日期
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 冲突数量
     */
    @Select("select count(*) from seat_reservation where seat_id = #{seatId} and date = #{date} and status not in ('已取消', '已完成') and ((start_time < #{endTime} and end_time > #{startTime}))")
    int checkSeatConflict(@Param("seatId") Long seatId,
                          @Param("date") LocalDate date,
                          @Param("startTime") String startTime,
                          @Param("endTime") String endTime);
}
