package com.hxq.smart_campus.controller.user;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.SeatReservationCreateDTO;
import com.hxq.smart_campus.entity.dto.SeatReservationResponseDTO;
import com.hxq.smart_campus.entity.vo.SeatListVO;
import com.hxq.smart_campus.entity.vo.SeatReservationDetailVO;
import com.hxq.smart_campus.entity.vo.SeatReservationListVO;
import com.hxq.smart_campus.entity.vo.SeatScheduleVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.SeatReservationService;
import com.hxq.smart_campus.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
        // 强制使用当前登录用户ID，防止越权查询
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("获取座位预约列表，参数：pageNum={}, pageSize={}, userId={}, seatId={}, date={}, status={}", pageNum, pageSize, currentUserId, seatId, date, status);
        PageInfo<SeatReservationListVO> seatReservationListVO = seatReservationService.getSeatReservationList(pageNum, pageSize, currentUserId, seatId, date, status);
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
        Long userId = SecurityUtils.getCurrentUserId();
        PageInfo<SeatReservationListVO> seatReservationListVO = seatReservationService.getMySeatReservationList(pageNum, pageSize, date, status, userId);
        return Result.success(seatReservationListVO);
    }

    /**
     * 获取可预约的座位列表
     * @return
     */
    @GetMapping("/available")
    @Operation(summary = "获取可预约的座位列表")
    public Result<List<SeatListVO>> getAvailableSeatList(
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) LocalDate date) {
        log.info("获取可预约的座位列表，参数：areaId={}, date={}", areaId, date);
        List<SeatListVO> seatListVO = seatReservationService.getAvailableSeatList(areaId, date);
        return Result.success(seatListVO);
    }

    /**
     * 获取座位预约详情
     * @param id 预约ID
     * @return
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取座位预约详情")
    public Result<SeatReservationDetailVO> getSeatReservationDetail(@PathVariable Long id) {
        log.info("获取座位预约详情，ID：{}", id);
        SeatReservationDetailVO detail = seatReservationService.getSeatReservationDetail(id);
        return Result.success(detail);
    }

    /**
     * 获取座位当日时段占用情况
     * @param seatId 座位ID
     * @param date 日期
     * @return
     */
    @GetMapping("/{seatId}/schedule/{date}")
    @Operation(summary = "获取座位当日时段占用情况")
    public Result<SeatScheduleVO> getSeatSchedule(
            @PathVariable Long seatId,
            @PathVariable String date) {
        log.info("获取座位时段，参数：seatId={}, date={}", seatId, date);
        SeatScheduleVO schedule = seatReservationService.getSeatSchedule(seatId, LocalDate.parse(date));
        return Result.success(schedule);
    }



}
