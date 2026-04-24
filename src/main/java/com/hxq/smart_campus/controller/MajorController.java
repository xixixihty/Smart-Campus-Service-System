package com.hxq.smart_campus.controller;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.MajorCreateDTO;
import com.hxq.smart_campus.entity.dto.MajorResponseDTO;
import com.hxq.smart_campus.entity.dto.MajorUpdateDTO;
import com.hxq.smart_campus.entity.vo.MajorDetailVO;
import com.hxq.smart_campus.entity.vo.MajorListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.MajorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/majors")
@Slf4j
@Tag(name = "专业管理模块")
public class MajorController {
    private final MajorService majorService;

    /**
     * 查询专业列表
     *
     * @param pageNum
     * @param pageSize
     * @param collegeId
     * @param majorName
     * @param status
     * @return
     */
    @GetMapping
    @Operation(summary = "查询专业列表")
    public Result<PageInfo<MajorListVO>> getMajorList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) String majorName,
            @RequestParam(required = false) String status
    ) {
        log.info("查询专业列表，参数：pageNum={}, pageSize={}, collegeId={}, majorName={}, status={}",
                pageNum, pageSize, collegeId, majorName, status);
        PageInfo<MajorListVO> pageInfo = majorService.getMajorList(
                pageNum, pageSize, collegeId, majorName, status);
        return Result.success(pageInfo);
    }
    /**
     * 新增专业
     *
     * @param majorCreateDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "新增专业")
    public Result<MajorResponseDTO> insertMajor(@RequestBody MajorCreateDTO majorCreateDTO) {
        log.info("新增专业，参数：{}", majorCreateDTO);
        MajorResponseDTO majorResponseDTO = majorService.insertMajor(majorCreateDTO);
        return Result.success(majorResponseDTO);

    }
    /**
     * 更新专业
     *
     * @param majorUpdateDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "更新专业")
    public Result<MajorResponseDTO> updateMajor(@RequestBody MajorUpdateDTO majorUpdateDTO) {
        log.info("更新专业，参数：{}", majorUpdateDTO);
        MajorResponseDTO majorResponseDTO = majorService.updateMajor(majorUpdateDTO);
        return Result.success(majorResponseDTO);
    }
    /**
     * 批量删除专业
     *
     * @param ids 专业ID列表
     * @return
     */
    @Operation(summary = "批量删除专业")
    @DeleteMapping("/{ids}")
    public Result<Boolean> deleteMajorByIds(@PathVariable List<Long> ids) {
        log.info("批量删除专业，参数：{}", ids);
        boolean b = majorService.deleteMajorByIds(ids);
        return Result.success(b);
    }
    /**
     * 查询专业详情
     *
     * @param id 专业ID
     * @return
     */
    @Operation(summary = "查询专业详情")
    @GetMapping("/{id}")
    public Result<MajorDetailVO> getMajorDetail(@PathVariable Long id) {
        log.info("查询专业详情，参数：{}", id);
        MajorDetailVO majorDetailVO = majorService.getMajorDetail(id);
        return Result.success(majorDetailVO);
    }
    /**
     * 更新专业状态
     *
     * @param id 专业ID
     * @return
     */
    @Operation(summary = "更新专业状态")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateMajorStatus(@PathVariable Long id) {
        log.info("更新专业状态，参数：{}", id);
        boolean b = majorService.updateMajorStatus(id);
        return Result.success(b);
    }
}
