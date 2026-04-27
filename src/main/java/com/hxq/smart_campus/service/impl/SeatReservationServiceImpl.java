package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.SeatReservationCreateDTO;
import com.hxq.smart_campus.entity.dto.SeatReservationResponseDTO;
import com.hxq.smart_campus.entity.vo.SeatReservationDetailVO;
import com.hxq.smart_campus.entity.vo.SeatReservationListVO;
import com.hxq.smart_campus.mapper.SeatReservationMapper;
import com.hxq.smart_campus.service.SeatReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeatReservationServiceImpl implements SeatReservationService {
    private final SeatReservationMapper seatReservationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeatReservationResponseDTO insertSeatReservation(SeatReservationCreateDTO seatReservationCreateDTO) {
        log.info("新增座位预约，参数：{}", seatReservationCreateDTO);
        if (seatReservationCreateDTO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (seatReservationCreateDTO.getSeatId() == null) {
            throw new IllegalArgumentException("座位ID不能为空");
        }
        if (seatReservationCreateDTO.getDate() == null) {
            throw new IllegalArgumentException("预约日期不能为空");
        }
        if (seatReservationCreateDTO.getStartTime() == null) {
            throw new IllegalArgumentException("开始时间不能为空");
        }
        if (seatReservationCreateDTO.getEndTime() == null) {
            throw new IllegalArgumentException("结束时间不能为空");
        }
        if (seatReservationCreateDTO.getStartTime().isAfter(seatReservationCreateDTO.getEndTime())) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        int conflictCount = seatReservationMapper.checkSeatConflict(
                seatReservationCreateDTO.getSeatId(),
                seatReservationCreateDTO.getDate(),
                seatReservationCreateDTO.getStartTime().format(formatter),
                seatReservationCreateDTO.getEndTime().format(formatter)
        );
        if (conflictCount > 0) {
            throw new IllegalArgumentException("该座位在该时间段已被预约");
        }

        int result = seatReservationMapper.insertSeatReservation(seatReservationCreateDTO);
        if (result <= 0) {
            throw new RuntimeException("新增座位预约失败");
        }
        Long id = seatReservationMapper.getLastInsertId();
        SeatReservationDetailVO seatReservationDetailVO = seatReservationMapper.getSeatReservationDetail(id);
        if (seatReservationDetailVO == null) {
            throw new RuntimeException("获取座位预约详情失败");
        }
        log.info("新增座位预约成功，ID：{}", id);
        return convertToSeatReservationResponseDTO(seatReservationDetailVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSeatReservation(Long id) {
        log.info("取消预约，参数：id={}", id);
        if (id == null) {
            throw new IllegalArgumentException("预约ID不能为空");
        }
        SeatReservationDetailVO existingReservation = seatReservationMapper.getSeatReservationDetail(id);
        if (existingReservation == null) {
            throw new IllegalArgumentException("预约记录不存在");
        }
        if ("使用中".equals(existingReservation.getStatus())) {
            throw new IllegalArgumentException("当前正在使用中，无法取消预约");
        }
        if ("已完成".equals(existingReservation.getStatus())) {
            throw new IllegalArgumentException("该预约已结束，无法取消");
        }
        int result = seatReservationMapper.deleteSeatReservation(id);
        if (result <= 0) {
            throw new RuntimeException("取消座位预约失败");
        }
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
        SeatReservationDetailVO existingReservation = seatReservationMapper.getSeatReservationDetail(id);
        if (existingReservation == null) {
            throw new IllegalArgumentException("预约记录不存在");
        }
        if (!"待签到".equals(existingReservation.getStatus())) {
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
        SeatReservationDetailVO existingReservation = seatReservationMapper.getSeatReservationDetail(id);
        if (existingReservation == null) {
            throw new IllegalArgumentException("预约记录不存在");
        }
        if (!"使用中".equals(existingReservation.getStatus()) && !"暂离".equals(existingReservation.getStatus())) {
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
        SeatReservationDetailVO existingReservation = seatReservationMapper.getSeatReservationDetail(id);
        if (existingReservation == null) {
            throw new IllegalArgumentException("预约记录不存在");
        }
        if (!"使用中".equals(existingReservation.getStatus())) {
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

    private SeatReservationResponseDTO convertToSeatReservationResponseDTO(SeatReservationDetailVO seatReservationDetailVO) {
        if (seatReservationDetailVO == null) {
            return null;
        }
        SeatReservationResponseDTO seatReservationResponseDTO = new SeatReservationResponseDTO();
        seatReservationResponseDTO.setId(seatReservationDetailVO.getId());
        seatReservationResponseDTO.setUserId(seatReservationDetailVO.getUserId());
        seatReservationResponseDTO.setUserName(seatReservationDetailVO.getUserName());
        seatReservationResponseDTO.setSeatId(seatReservationDetailVO.getSeatId());
        seatReservationResponseDTO.setSeatNumber(seatReservationDetailVO.getSeatNumber());
        seatReservationResponseDTO.setRoomId(seatReservationDetailVO.getRoomId());
        seatReservationResponseDTO.setRoomName(seatReservationDetailVO.getRoomName());
        seatReservationResponseDTO.setDate(seatReservationDetailVO.getDate());
        seatReservationResponseDTO.setStartTime(seatReservationDetailVO.getStartTime());
        seatReservationResponseDTO.setEndTime(seatReservationDetailVO.getEndTime());
        seatReservationResponseDTO.setLeaveTime(seatReservationDetailVO.getLeaveTime());
        seatReservationResponseDTO.setStatus(seatReservationDetailVO.getStatus());
        return seatReservationResponseDTO;
    }
}
