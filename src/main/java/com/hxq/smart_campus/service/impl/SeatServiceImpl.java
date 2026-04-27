package com.hxq.smart_campus.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.SeatCreateDTO;
import com.hxq.smart_campus.entity.dto.SeatResponseDTO;
import com.hxq.smart_campus.entity.dto.SeatUpdateDTO;
import com.hxq.smart_campus.entity.vo.SeatDetailVO;
import com.hxq.smart_campus.entity.vo.SeatListVO;
import com.hxq.smart_campus.mapper.SeatMapper;
import com.hxq.smart_campus.service.SeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatServiceImpl implements SeatService {
    private final SeatMapper seatMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeatResponseDTO insertSeat(SeatCreateDTO seatCreateDTO) {
        log.info("新增座位，参数：{}", seatCreateDTO);
        if (seatCreateDTO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (seatCreateDTO.getRoomId() == null) {
            throw new IllegalArgumentException("阅览室ID不能为空");
        }
        if (seatCreateDTO.getSeatNumber() == null || seatCreateDTO.getSeatNumber().isEmpty()) {
            throw new IllegalArgumentException("座位编号不能为空");
        }
        int result = seatMapper.insertSeat(seatCreateDTO);
        if (result <= 0) {
            throw new RuntimeException("新建座位失败");
        }
        Long id = seatMapper.getLastInsertId();
        SeatDetailVO seatDetailVO = seatMapper.getSeatDetail(id);
        if (seatDetailVO == null) {
            throw new RuntimeException("新建座位失败");
        }
        log.info("新增座位成功，ID：{}", id);
        return convertToSeatResponseDTO(seatDetailVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeatResponseDTO updateSeat(SeatUpdateDTO seatUpdateDTO) {
        log.info("更新座位，参数：{}", seatUpdateDTO);
        if (seatUpdateDTO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (seatUpdateDTO.getId() == null) {
            throw new IllegalArgumentException("座位ID不能为空");
        }
        SeatDetailVO existingSeat = seatMapper.getSeatDetail(seatUpdateDTO.getId());
        if (existingSeat == null) {
            throw new IllegalArgumentException("座位不存在");
        }
        int result = seatMapper.updateSeat(seatUpdateDTO);
        if (result <= 0) {
            throw new RuntimeException("更新座位失败");
        }
        SeatDetailVO seatDetailVO = seatMapper.getSeatDetail(seatUpdateDTO.getId());
        log.info("更新座位成功，ID：{}", seatUpdateDTO.getId());
        return convertToSeatResponseDTO(seatDetailVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSeat(List<Long> ids) {
        log.info("删除座位，ID列表：{}", ids);
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空");
        }
        for (Long id : ids) {
            SeatDetailVO seatDetailVO = seatMapper.getSeatDetail(id);
            if (seatDetailVO == null) {
                throw new IllegalArgumentException("删除座位失败，座位不存在，ID：" + id);
            }
            if (!"空闲".equals(seatDetailVO.getStatus()) && !"维修中".equals(seatDetailVO.getStatus())) {
                throw new IllegalArgumentException("删除座位失败，座位状态不是空闲或维修中，ID：" + id);
            }
        }
        int result = seatMapper.deleteBatch(ids);
        if (result <= 0) {
            throw new RuntimeException("删除座位失败");
        }
        log.info("删除座位成功，数量：{}", result);
        return true;
    }

    @Override
    public PageInfo<SeatListVO> getSeatList(Integer pageNum, Integer pageSize, Long roomId, String status) {
        log.info("获取座位列表，参数：pageNum={}, pageSize={}, roomId={}, status={}", pageNum, pageSize, roomId, status);
        PageHelper.startPage(pageNum, pageSize);
        List<SeatListVO> list = seatMapper.getSeatList(roomId, status);
        log.info("获取座位列表成功，数量：{}", list.size());
        return new PageInfo<>(list);
    }

    @Override
    public SeatDetailVO getSeatDetail(Long id) {
        log.info("获取座位详情，参数：id={}", id);
        if (id == null) {
            throw new IllegalArgumentException("座位ID不能为空");
        }
        SeatDetailVO seatDetailVO = seatMapper.getSeatDetail(id);
        if (seatDetailVO == null) {
            throw new IllegalArgumentException("座位不存在");
        }
        return seatDetailVO;
    }

    private SeatResponseDTO convertToSeatResponseDTO(SeatDetailVO seatDetailVO) {
        if (seatDetailVO == null) {
            return null;
        }
        SeatResponseDTO seatResponseDTO = new SeatResponseDTO();
        seatResponseDTO.setId(seatDetailVO.getId());
        seatResponseDTO.setRoomId(seatDetailVO.getRoomId());
        seatResponseDTO.setSeatNumber(seatDetailVO.getSeatNumber());
        seatResponseDTO.setStatus(seatDetailVO.getStatus());
        return seatResponseDTO;
    }
}
