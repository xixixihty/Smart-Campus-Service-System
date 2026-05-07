package com.hxq.smart_campus.controller.user;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.SeatReservationCreateDTO;
import com.hxq.smart_campus.entity.dto.SeatReservationResponseDTO;
import com.hxq.smart_campus.entity.vo.SeatReservationListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.SeatReservationService;
import com.hxq.smart_campus.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/seat-reservations/user")
@Tag(name = "座位预约管理")
@RequiredArgsConstructor
@Slf4j
public class SeatReservationController {
    private final SeatReservationService seatReservationService;

    /**
     * 新增座位预约
     * @param seatReservationCreateDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "预约座位")
    public Result<SeatReservationResponseDTO> insertSeatReservation(@RequestBody SeatReservationCreateDTO seatReservationCreateDTO) {
        log.info("新增座位预约，参数：{}", seatReservationCreateDTO);
        SeatReservationResponseDTO seatReservationResponseDTO = seatReservationService.insertSeatReservation(seatReservationCreateDTO);
        return Result.success(seatReservationResponseDTO);
    }

    /**
     * 取消座位预约
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "取消预约")
    public Result<Boolean> deleteSeatReservation(@PathVariable Long id) {
        log.info("取消预约座位，预约ID：{}", id);
        boolean b = seatReservationService.deleteSeatReservation(id);
        return Result.success(b);
    }

    /**
     * 签到座位预约
     * @param id
     * @return
     */
    @PostMapping("/{id}/check-in")
    @Operation(summary = "签到")
    public Result<Boolean> checkInSeatReservation(@PathVariable Long id) {
        log.info("签到座位，ID：{}", id);
        boolean b = seatReservationService.checkInSeatReservation(id);
        return Result.success(b);
    }

    /**
     * 签退座位预约
     * @param id
     * @return
     */
    @PostMapping("/{id}/check-out")
    @Operation(summary = "签退")
    public Result<Boolean> checkOutSeatReservation(@PathVariable Long id) {
        log.info("签退座位，ID：{}", id);
        boolean b = seatReservationService.checkOutSeatReservation(id);
        return Result.success(b);
    }

    /**
     * 暂离座位预约
     * @param id
     * @return
     */
    @PostMapping("/{id}/leave")
    @Operation(summary = "暂离")
    public Result<Boolean> leaveSeatReservation(@PathVariable Long id) {
        log.info("暂离座位，ID：{}", id);
        boolean b = seatReservationService.leaveSeatReservation(id);
        return Result.success(b);
    }

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
    @GetMapping
    @Operation(summary = "获取座位预约列表")
    public Result<PageInfo<SeatReservationListVO>> getSeatReservationList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long seatId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String status
    ) {
        log.info("获取座位预约列表，参数：pageNum={}, pageSize={}, userId={}, seatId={}, date={}, status={}", pageNum, pageSize, userId, seatId, date, status);
        PageInfo<SeatReservationListVO> seatReservationListVO = seatReservationService.getSeatReservationList(pageNum, pageSize, userId, seatId, date, status);
        return Result.success(seatReservationListVO);
    }

    /**
     * 获取我的座位预约列表
     * @param pageNum
     * @param pageSize
     * @param date
     * @param status
     * @return
     */
    
    @GetMapping("/my")
    @Operation(summary = "获取我的座位预约列表")
    public Result<PageInfo<SeatReservationListVO>> getMySeatReservationList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String status
    ) {
        log.info("获取我的座位预约列表，参数：pageNum={}, pageSize={}, date={}, status={}", pageNum, pageSize, date, status );
        // TODO :从登陆信息中获取到用户ID
        Long userId = SecurityUtils.getCurrentUserId();
        PageInfo<SeatReservationListVO> seatReservationListVO = seatReservationService.getMySeatReservationList(pageNum, pageSize, date, status, userId);
        return Result.success(seatReservationListVO);
    }
}
