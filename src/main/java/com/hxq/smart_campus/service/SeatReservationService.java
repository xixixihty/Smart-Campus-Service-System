package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.SeatReservationCreateDTO;
import com.hxq.smart_campus.entity.dto.SeatReservationResponseDTO;
import com.hxq.smart_campus.entity.vo.SeatListVO;
import com.hxq.smart_campus.entity.vo.SeatReservationDetailVO;
import com.hxq.smart_campus.entity.vo.SeatReservationListVO;
import com.hxq.smart_campus.entity.vo.SeatScheduleVO;

import java.time.LocalDate;
import java.util.List;

public interface SeatReservationService {
    /**
     * 新增座位预约
     * @param seatReservationCreateDTO
     * @return
     */
    SeatReservationResponseDTO insertSeatReservation(SeatReservationCreateDTO seatReservationCreateDTO);

    /**
     * 删除座位预约
     * @param id
     * @return
     */
    boolean deleteSeatReservation(Long id);

    /**
     * 签到座位预约
     * @param id
     * @return
     */
    boolean checkInSeatReservation(Long id);
    /**
     * 签退座位预约
     * @param id
     * @return
     */
    boolean checkOutSeatReservation(Long id);
    /**
     * 签退/暂离座位预约
     * @param id
     * @return
     */
    boolean leaveSeatReservation(Long id);
    /**
     * 获取座位预约列表
     * @param pageNum
     * @param pageSize
     * @param userId
     * @param seatId
     * @param date
     * @param status
     * @return
     */
    PageInfo<SeatReservationListVO> getSeatReservationList(Integer pageNum, Integer pageSize, Long userId, Long seatId, LocalDate date, String status);

    /**
     * 获取我的座位预约列表
     * @param pageNum
     * @param pageSize
     * @param date
     * @param status
     * @param userId
     * @return
     */
    PageInfo<SeatReservationListVO> getMySeatReservationList(Integer pageNum, Integer pageSize, LocalDate date, String status, Long userId);

    /**
     * 获取可用座位列表
     * @return
     */
    List<SeatListVO> getAvailableSeatList();

    /**
     * 获取可用座位列表（支持区域和日期筛选）
     * @param areaId 区域ID
     * @param date 日期
     * @return
     */
    List<SeatListVO> getAvailableSeatList(Long areaId, LocalDate date);

    /**
     * 获取座位当日时段占用情况
     * @param seatId 座位ID
     * @param date 日期
     * @return
     */
    SeatScheduleVO getSeatSchedule(Long seatId, LocalDate date);

    /**
     * 获取座位预约详情
     * @param id 预约ID
     * @return
     */
    SeatReservationDetailVO getSeatReservationDetail(Long id);
}
