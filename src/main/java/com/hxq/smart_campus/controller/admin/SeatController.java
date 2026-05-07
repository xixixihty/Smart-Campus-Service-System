package com.hxq.smart_campus.controller.admin;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.SeatCreateDTO;
import com.hxq.smart_campus.entity.dto.SeatResponseDTO;
import com.hxq.smart_campus.entity.dto.SeatUpdateDTO;
import com.hxq.smart_campus.entity.vo.SeatDetailVO;
import com.hxq.smart_campus.entity.vo.SeatListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "座位管理")
public class SeatController {
    private final SeatService seatService;

    /**
     * 新增座位
     * @param seatCreateDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "新增座位")
    public Result<SeatResponseDTO> insertSeat(@RequestBody SeatCreateDTO seatCreateDTO) {
        log.info("新增座位: {}", seatCreateDTO);
        SeatResponseDTO seatResponseDTO = seatService.insertSeat(seatCreateDTO);
        return Result.success(seatResponseDTO);
    }

    /**
     * 更新座位
     * @param seatUpdateDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "更新座位")
    public Result<SeatResponseDTO> updateSeat(@RequestBody SeatUpdateDTO seatUpdateDTO) {
        log.info("更新座位: {}", seatUpdateDTO);
        SeatResponseDTO seatResponseDTO = seatService.updateSeat(seatUpdateDTO);
        return Result.success(seatResponseDTO);
    }

    /**
     * 删除座位
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除座位")
    public Result<Boolean> deleteSeat(@PathVariable List<Long> ids) {
        log.info("删除座位: {}", ids);
        boolean b = seatService.deleteSeat(ids);
        return Result.success(b);
    }

    /**
     * 获取座位列表
     * @param pageNum
     * @param pageSize
     * @param roomId
     * @param status
     * @return
     */
    @GetMapping
    @Operation(summary = "获取座位列表")
    public Result<PageInfo<SeatListVO>> getSeatList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String status
    ) {
        log.info("获取座位列表: pageNum={}, pageSize={}, roomId={}, status={}", pageNum, pageSize, roomId, status);
        PageInfo<SeatListVO> pageInfo = seatService.getSeatList(pageNum, pageSize, roomId, status);
        return Result.success(pageInfo);
    }

    /**
     * 获取座位详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取座位详情")
    public Result<SeatDetailVO> getSeatDetail(@PathVariable Long id) {
        log.info("获取座位详情: {}", id);
        SeatDetailVO seatDetailVO = seatService.getSeatDetail(id);
        return Result.success(seatDetailVO);
    }
}
