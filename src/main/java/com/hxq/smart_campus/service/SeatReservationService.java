package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.SeatReservationCreateDTO;
import com.hxq.smart_campus.entity.dto.SeatReservationResponseDTO;
import com.hxq.smart_campus.entity.vo.SeatReservationListVO;

import java.time.LocalDate;

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
}
