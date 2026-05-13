package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.SeatReservationCreateDTO;
import com.hxq.smart_campus.entity.dto.SeatReservationMessage;
import com.hxq.smart_campus.entity.dto.SeatReservationResponseDTO;
import com.hxq.smart_campus.entity.vo.SeatDetailVO;
import com.hxq.smart_campus.entity.vo.SeatListVO;
import com.hxq.smart_campus.entity.vo.SeatReservationDetailVO;
import com.hxq.smart_campus.entity.vo.SeatReservationListVO;
import com.hxq.smart_campus.entity.vo.SeatScheduleVO;
import com.hxq.smart_campus.exception.BusinessException;
import com.hxq.smart_campus.mapper.SeatMapper;
import com.hxq.smart_campus.mapper.SeatReservationMapper;
import com.hxq.smart_campus.service.SeatReservationService;
import com.hxq.smart_campus.service.mq.SeatReservationProducer;
import com.hxq.smart_campus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static com.hxq.smart_campus.constant.MessageConstant.SEAT_STATUS_FREE;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeatReservationServiceImpl implements SeatReservationService {
    private final SeatReservationMapper seatReservationMapper;
    private final RedisSeatService redisSeatService;
    private final SeatReservationProducer seatReservationProducer;
    private final SeatMapper seatMapper;

    @Override
    public SeatReservationResponseDTO insertSeatReservation(SeatReservationCreateDTO dto) {
        log.info("新增座位预约，参数：{}", dto);
        if (dto == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (dto.getSeatId() == null) {
            throw new IllegalArgumentException("座位ID不能为空");
        }
        if (dto.getDate() == null) {
            throw new IllegalArgumentException("预约日期不能为空");
        }
        if (dto.getStartTime() == null) {
            throw new IllegalArgumentException("开始时间不能为空");
        }
        if (dto.getEndTime() == null) {
            throw new IllegalArgumentException("结束时间不能为空");
        }
        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }

        Long userId = SecurityUtils.getCurrentUserId();
        dto.setUserId(userId);

        // 1. 布隆过滤器前置校验
        if (!redisSeatService.checkSeatExists(dto.getSeatId())) {
            throw new BusinessException("SEAT_NOT_EXIST", "座位不存在");
        }

        // 2. 生成预约编号并执行 Lua 脚本原子预约
        String reservationNo = generateReservationNo();
        dto.setReservationNo(reservationNo);

        Long result = redisSeatService.reserveSeat(
                dto.getSeatId(), userId, dto.getDate(),
                dto.getStartTime(), dto.getEndTime(), reservationNo);
        if (result == 0) {
            throw new BusinessException("TIME_CONFLICT", "该座位在该时间段已被预约");
        } else if (result != 1) {
            throw new RuntimeException("预约失败，请稍后再试");
        }

        // 3. 发送 MQ 消息异步落库
        SeatReservationMessage msg = new SeatReservationMessage(
                dto.getSeatId(), userId, dto.getDate(),
                dto.getStartTime(), dto.getEndTime(), reservationNo, "RESERVE");
        seatReservationProducer.sendReserveMessage(msg);

        // 4. 返回响应
        SeatReservationResponseDTO response = new SeatReservationResponseDTO();
        response.setReservationNo(reservationNo);
        response.setSeatId(dto.getSeatId());
        response.setUserId(userId);
        response.setDate(dto.getDate());
        response.setStartTime(dto.getStartTime());
        response.setEndTime(dto.getEndTime());
        response.setStatus("待签到");
        return response;
    }

    @Override
    public boolean deleteSeatReservation(Long id) {
        log.info("取消预约，参数：id={}", id);
        if (id == null) {
            throw new IllegalArgumentException("预约ID不能为空");
        }

        SeatReservationDetailVO existing = seatReservationMapper.getSeatReservationDetail(id);
        if (existing == null) {
            throw new IllegalArgumentException("预约记录不存在");
        }
        if ("使用中".equals(existing.getStatus())) {
            throw new IllegalArgumentException("当前正在使用中，无法取消预约");
        }
        if ("已完成".equals(existing.getStatus())) {
            throw new IllegalArgumentException("该预约已结束，无法取消");
        }

        // 1. 执行 Redis Lua 取消预约
        Long result = redisSeatService.cancelReservation(
                existing.getSeatId(), existing.getUserId(),
                existing.getDate(), existing.getStartTime());
        if (result == -1) {
            log.warn("Redis取消预约未找到记录: reservationNo={}", existing.getReservationNo());
        }

        // 2. 发送 MQ 消息更新 DB
        SeatReservationMessage msg = new SeatReservationMessage(
                existing.getSeatId(), existing.getUserId(), existing.getDate(),
                existing.getStartTime(), existing.getEndTime(),
                existing.getReservationNo(), "CANCEL");
        seatReservationProducer.sendCancelMessage(msg);

        log.info("取消预约成功，ID：{}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkInSeatReservation(Long id) {
        log.info("签到，参数：id={}", id);
        if (id == null) {
            throw new IllegalArgumentException("预约ID不能为空");
        }
        SeatReservationDetailVO existing = seatReservationMapper.getSeatReservationDetail(id);
        if (existing == null) {
            throw new IllegalArgumentException("预约记录不存在");
        }
        if (!"待签到".equals(existing.getStatus())) {
            throw new IllegalArgumentException("当前状态不是待签到，无法签到");
        }
        int result = seatReservationMapper.checkInSeatReservation(id);
        if (result <= 0) {
            throw new RuntimeException("签到失败");
        }
        log.info("签到成功，ID：{}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkOutSeatReservation(Long id) {
        log.info("签退，参数：id={}", id);
        if (id == null) {
            throw new IllegalArgumentException("预约ID不能为空");
        }
        SeatReservationDetailVO existing = seatReservationMapper.getSeatReservationDetail(id);
        if (existing == null) {
            throw new IllegalArgumentException("预约记录不存在");
        }
        if (!"使用中".equals(existing.getStatus()) && !"暂离".equals(existing.getStatus())) {
            throw new IllegalArgumentException("当前状态不是使用中或暂离，无法签退");
        }
        int result = seatReservationMapper.checkOutSeatReservation(id);
        if (result <= 0) {
            throw new RuntimeException("签退失败");
        }
        log.info("签退成功，ID：{}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean leaveSeatReservation(Long id) {
        log.info("暂离，参数：id={}", id);
        if (id == null) {
            throw new IllegalArgumentException("预约ID不能为空");
        }
        SeatReservationDetailVO existing = seatReservationMapper.getSeatReservationDetail(id);
        if (existing == null) {
            throw new IllegalArgumentException("预约记录不存在");
        }
        if (!"使用中".equals(existing.getStatus())) {
            throw new IllegalArgumentException("当前状态不是使用中，无法暂离");
        }
        int result = seatReservationMapper.leaveSeatReservation(id);
        if (result <= 0) {
            throw new RuntimeException("暂离失败");
        }
        log.info("暂离成功，ID：{}", id);
        return true;
    }

    @Override
    public PageInfo<SeatReservationListVO> getSeatReservationList(Integer pageNum, Integer pageSize, Long userId, Long seatId, LocalDate date, String status) {
        log.info("获取预约列表，参数：pageNum={}, pageSize={}, userId={}, seatId={}, date={}, status={}",
                pageNum, pageSize, userId, seatId, date, status);
        PageHelper.startPage(pageNum, pageSize);
        List<SeatReservationListVO> list = seatReservationMapper.getSeatReservationList(userId, seatId, date, status);
        log.info("获取预约列表成功，数量：{}", list.size());
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<SeatReservationListVO> getMySeatReservationList(Integer pageNum, Integer pageSize, LocalDate date, String status, Long userId) {
        log.info("获取我的预约列表，参数：pageNum={}, pageSize={}, date={}, status={}, userId={}",
                pageNum, pageSize, date, status, userId);
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        PageHelper.startPage(pageNum, pageSize);
        List<SeatReservationListVO> list = seatReservationMapper.getSeatReservationList(userId, null, date, status);
        log.info("获取我的预约列表成功，数量：{}", list.size());
        return new PageInfo<>(list);
    }

    /**
     * 获取可用座位列表（包含空闲和使用中的座位）
     * @return
     */
    @Override
    public List<SeatListVO> getAvailableSeatList() {
        return seatMapper.getAvailableSeatList(null);
    }

    @Override
    public List<SeatListVO> getAvailableSeatList(Long areaId, LocalDate date) {
        return seatMapper.getSeatList(areaId, null);
    }

    @Override
    public SeatScheduleVO getSeatSchedule(Long seatId, LocalDate date) {
        SeatScheduleVO schedule = new SeatScheduleVO();
        schedule.setSeatId(seatId);
        
        SeatDetailVO seatDetail = seatMapper.getSeatDetail(seatId);
        if (seatDetail != null) {
            schedule.setSeatNumber(seatDetail.getSeatNumber());
        }
        schedule.setDate(date);
        
        List<SeatReservationListVO> reservations = seatReservationMapper.getSeatReservationList(null, seatId, date, null);
        
        List<SeatScheduleVO.TimeSlotVO> timeSlots = new java.util.ArrayList<>();
        String[] times = {"08:00", "09:00", "10:00", "11:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00"};
        
        for (String time : times) {
            SeatScheduleVO.TimeSlotVO slot = new SeatScheduleVO.TimeSlotVO();
            slot.setTime(time);
            slot.setAvailable(true);
            
            LocalTime slotTime = LocalTime.parse(time);
            
            for (SeatReservationListVO res : reservations) {
                if (slotTime.isAfter(res.getStartTime()) && slotTime.isBefore(res.getEndTime())) {
                    slot.setAvailable(false);
                    slot.setReservedBy(res.getUserName());
                    break;
                }
            }
            
            timeSlots.add(slot);
        }
        
        schedule.setTimeSlots(timeSlots);
        return schedule;
    }

    private String generateReservationNo() {
        return "SEAT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
