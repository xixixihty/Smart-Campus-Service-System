package com.hxq.smart_campus.controller;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.CollegeCreateDTO;
import com.hxq.smart_campus.entity.dto.CollegeResponseDTO;
import com.hxq.smart_campus.entity.dto.CollegeUpdateDTO;
import com.hxq.smart_campus.entity.pojo.College;
import com.hxq.smart_campus.entity.vo.CollegeDetailVO;
import com.hxq.smart_campus.entity.vo.CollegeListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.CollegeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("/colleges")
@Tag(name = "学院管理模块")
public class CollegeController {

    private final CollegeService collegeService;


    /**
     * 获取学院列表
     */
    @GetMapping
    @Operation(summary = "获取学院列表")
    public Result<PageInfo<CollegeListVO>> getCollegeList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String collegeName,
            @RequestParam(required = false) String status
    ) {
        log.info("查询学院列表 page: {}, size: {}, collegeName: {} , status: {}", pageNum, pageSize, collegeName, status);
        PageInfo<CollegeListVO> collegeVOList = collegeService.getCollegeList(pageNum, pageSize, collegeName, status);
        return Result.success(collegeVOList);
    }
    /**
     * 获取学院详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取学院详情")
    public Result<CollegeDetailVO> getCollegeDetail(@PathVariable Long id) {
        log.info("查询学院详情 id: {}", id);
        CollegeDetailVO collegeDetailVO = collegeService.getCollegeDetail(id);
        return Result.success(collegeDetailVO);
    }
    /**
     * 创建学院
     */
    @PostMapping
    @Operation(summary = "创建学院")
    public Result<CollegeResponseDTO> createCollege(@RequestBody CollegeCreateDTO collegeCreateDTO) {
        log.info("创建学院 collegeCreateDTO: {}", collegeCreateDTO);
        CollegeResponseDTO collegeResponseDTO = collegeService.createCollege(collegeCreateDTO);
        return Result.success(collegeResponseDTO);
    }
    /**
     * 更新学院
     */
    @PutMapping
    @Operation(summary = "更新学院")
    public Result<CollegeResponseDTO> updateCollege(@RequestBody CollegeUpdateDTO collegeUpdateDTO) {
        log.info("更新学院 collegeUpdateDTO: {}", collegeUpdateDTO);
        CollegeResponseDTO collegeResponseDTO = collegeService.updateCollege(collegeUpdateDTO);
        return Result.success(collegeResponseDTO);
    }
    /**
     * 删除学院
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除学院")
    public Result<Boolean> deleteCollege(@PathVariable List<Long> ids) {
        log.info("删除学院 ids: {}", ids);
        boolean result = collegeService.deleteCollege(ids);
        return Result.success(result);
    }
    /**
     * 更新学院状态
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新学院状态")
    public Result<Boolean> updateStatus(@PathVariable Long id) {
        log.info("更新学院状态 id: {}", id);
        boolean result = collegeService.updateStatus(id);
        return Result.success(result);
    }
}
