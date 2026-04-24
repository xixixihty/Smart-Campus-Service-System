package com.hxq.smart_campus.controller;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.CollegeCreateDTO;
import com.hxq.smart_campus.entity.dto.CollegeResponseDTO;
import com.hxq.smart_campus.entity.dto.CollegeUpdateDTO;
import com.hxq.smart_campus.entity.vo.CollegeDetailVO;
import com.hxq.smart_campus.entity.vo.CollegeListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.CollegeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学院管理控制器
 *
 * @author XiongQi He
 * @since 2026-04-21
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/colleges")
@Tag(name = "学院管理模块")
public class CollegeController {

    private final CollegeService collegeService;

    /**
     * 获取学院列表
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param collegeName 学院名称（可选）
     * @param status 状态（可选）
     * @return 学院列表分页信息
     */
    @GetMapping
    @Operation(summary = "获取学院列表")
    public Result<PageInfo<CollegeListVO>> getCollegeList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String collegeName,
            @RequestParam(required = false) String status
    ) {
        log.info("获取学院列表，页码：{}，每页数量：{}，学院名称：{}，状态：{}", pageNum, pageSize, collegeName, status);
        PageInfo<CollegeListVO> collegeVOList = collegeService.getCollegeList(pageNum, pageSize, collegeName, status);
        return Result.success(collegeVOList);
    }

    /**
     * 获取学院详情
     *
     * @param id 学院ID
     * @return 学院详情信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取学院详情")
    public Result<CollegeDetailVO> getCollegeDetail(@PathVariable Long id) {
        log.info("获取学院详情，学院ID：{}", id);
        CollegeDetailVO collegeDetailVO = collegeService.getCollegeDetail(id);
        return Result.success(collegeDetailVO);
    }

    /**
     * 创建学院
     *
     * @param collegeCreateDTO 学院创建DTO
     * @return 创建的学院信息
     */
    @PostMapping
    @Operation(summary = "创建学院")
    public Result<CollegeResponseDTO> createCollege(@RequestBody CollegeCreateDTO collegeCreateDTO) {
        log.info("创建学院，学院信息：{}", collegeCreateDTO);
        CollegeResponseDTO collegeResponseDTO = collegeService.createCollege(collegeCreateDTO);
        return Result.success(collegeResponseDTO);
    }

    /**
     * 更新学院
     *
     * @param collegeUpdateDTO 学院更新DTO
     * @return 更新后的学院信息
     */
    @PutMapping
    @Operation(summary = "更新学院")
    public Result<CollegeResponseDTO> updateCollege(@RequestBody CollegeUpdateDTO collegeUpdateDTO) {
        log.info("更新学院，学院信息：{}", collegeUpdateDTO);
        CollegeResponseDTO collegeResponseDTO = collegeService.updateCollege(collegeUpdateDTO);
        return Result.success(collegeResponseDTO);
    }

    /**
     * 删除学院
     *
     * @param ids 学院ID列表
     * @return 删除结果
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除学院")
    public Result<Boolean> deleteCollege(@PathVariable List<Long> ids) {
        log.info("删除学院，学院ID列表：{}", ids);
        boolean result = collegeService.deleteCollege(ids);
        return Result.success(result);
    }
    /**
     * 更新学院状态
     *
     * @param id 学院ID
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新学院状态")
    public Result<Boolean> updateStatus(@PathVariable Long id) {
        log.info("更新学院状态，学院ID：{}", id);
        boolean result = collegeService.updateStatus(id);
        return Result.success(result);
    }
}