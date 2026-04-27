package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.SeatCreateDTO;
import com.hxq.smart_campus.entity.dto.SeatResponseDTO;
import com.hxq.smart_campus.entity.dto.SeatUpdateDTO;
import com.hxq.smart_campus.entity.vo.SeatDetailVO;
import com.hxq.smart_campus.entity.vo.SeatListVO;

import java.util.List;

public interface SeatService {
    /**
     * 新增座位
     * @param seatCreateDTO 座位创建DTO
     * @return 座位响应DTO
     */
    SeatResponseDTO insertSeat(SeatCreateDTO seatCreateDTO);

    /**
     * 更新座位
     * @param seatUpdateDTO 座位更新DTO
     * @return 座位响应DTO
     */
    SeatResponseDTO updateSeat(SeatUpdateDTO seatUpdateDTO);

    /**
     * 删除座位
     * @param ids 座位ID列表
     * @return 是否成功
     */
    boolean deleteSeat(List<Long> ids);

    /**
     * 获取座位列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param roomId 阅览室ID
     * @param status 状态
     * @return 座位分页列表
     */
    PageInfo<SeatListVO> getSeatList(Integer pageNum, Integer pageSize, Long roomId, String status);

    /**
     * 获取座位详情
     * @param id 座位ID
     * @return 座位详情VO
     */
    SeatDetailVO getSeatDetail(Long id);
}
